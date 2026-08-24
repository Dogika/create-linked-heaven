package com.dogika.lh.client;

import com.dogika.lh.LinkedHeaven;
import com.dogika.lh.network.CreateGroupPayload;
import com.dogika.lh.network.GroupEntry;
import com.dogika.lh.network.SelectGroupPayload;
import com.dogika.lh.network.SetGroupLockedPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FrequencyInterfaceScreen extends Screen {

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 210;
    private static final int ROW_HEIGHT = 28;
    private static final int LOCK_BUTTON_WIDTH = 44;

    private List<GroupEntry> groups;
    private UUID selectedGroupId;

    private EditBox nameBox;
    private Button createButton;
    private Button lockAtCreationButton;
    private boolean lockAtCreation;

    private int listTop;
    private int listBottom;
    private int scrollOffset;
    private List<GroupEntry> visibleRows = List.of();

    public FrequencyInterfaceScreen(List<GroupEntry> groups, UUID selectedGroupId) {
        super(Component.translatable("menu."+ LinkedHeaven.MODID+".title"));
        this.groups = groups;
        this.selectedGroupId = selectedGroupId;
    }

    public void updateData(List<GroupEntry> newGroups, UUID newSelectedGroupId) {
        this.groups = newGroups;
        this.selectedGroupId = newSelectedGroupId;
        refreshFilter();
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        nameBox = new EditBox(this.font, left + 10, top + 26, PANEL_WIDTH - 20, 18,
            Component.translatable("menu."+ LinkedHeaven.MODID+".name_box"));
        nameBox.setMaxLength(48);
        nameBox.setResponder(s -> refreshFilter());
        addRenderableWidget(nameBox);
        setInitialFocus(nameBox);

        lockAtCreationButton = Button.builder(lockAtCreationLabel(), b -> {
                lockAtCreation = !lockAtCreation;
                b.setMessage(lockAtCreationLabel());
            })
            .bounds(left + 10, top + 50, 100, 18)
            .build();
        addRenderableWidget(lockAtCreationButton);

        createButton = Button.builder(Component.translatable("menu."+ LinkedHeaven.MODID+".create"), b -> {
                String name = nameBox.getValue().strip();
                if (!name.isEmpty()) {
                    PacketDistributor.sendToServer(new CreateGroupPayload(name, lockAtCreation));
                }
                nameBox.setValue("");
            })
            .bounds(left + 116, top + 50, PANEL_WIDTH - 126, 18)
            .build();
        addRenderableWidget(createButton);

        listTop = top + 74;
        listBottom = top + PANEL_HEIGHT - 10;

        refreshFilter();
    }

    private Component lockAtCreationLabel() {
        return Component.translatable(lockAtCreation
            ? "menu."+ LinkedHeaven.MODID+".lock_new_on"
            : "menu."+ LinkedHeaven.MODID+".lock_new_off");
    }

    private void refreshFilter() {
        String query = nameBox == null ? "" : nameBox.getValue().strip().toLowerCase(Locale.ROOT);
        List<GroupEntry> rows = new ArrayList<>();
        for (GroupEntry entry : groups) {
            if (query.isEmpty() || entry.name().toLowerCase(Locale.ROOT).contains(query)
                || entry.name().equalsIgnoreCase("global") && "global".contains(query)) {
                rows.add(entry);
            }
        }
        this.visibleRows = rows;
        this.scrollOffset = 0;

        boolean exactDuplicate = !query.isEmpty() && groups.stream()
            .anyMatch(g -> g.name().equalsIgnoreCase(nameBox.getValue().strip()));
        boolean canCreate = !query.isEmpty() && !exactDuplicate;
        if (createButton != null) {
            createButton.visible = canCreate;
            createButton.active = canCreate;
        }
        if (lockAtCreationButton != null) {
            lockAtCreationButton.visible = canCreate;
            lockAtCreationButton.active = canCreate;
        }
    }

    private boolean showingDuplicateWarning() {
        String typed = nameBox.getValue().strip();
        return !typed.isEmpty() && groups.stream().anyMatch(g -> g.name().equalsIgnoreCase(typed));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xC0101015);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        graphics.drawString(this.font, this.title, left + 10, top + 10, 0xFFFFFF, false);

        if (showingDuplicateWarning()) {
            graphics.drawString(this.font,
                    Component.translatable("menu."+ LinkedHeaven.MODID+".duplicate", nameBox.getValue().strip()),
                    left + 10, top + 51, 0xFF5555, false);
        }

        renderList(graphics, left, mouseX, mouseY);
    }

    private void renderList(GuiGraphics graphics, int left, int mouseX, int mouseY) {
        graphics.enableScissor(left, listTop, left + PANEL_WIDTH, listBottom);

        int y = listTop - scrollOffset;
        for (GroupEntry entry : visibleRows) {
            if (y + ROW_HEIGHT >= listTop && y <= listBottom) {
                renderRow(graphics, left, y, entry, mouseX, mouseY);
            }
            y += ROW_HEIGHT;
        }

        graphics.disableScissor();

        if (contentHeight() > (listBottom - listTop)) {
            renderScrollbar(graphics, left);
        }
    }

    private void renderRow(GuiGraphics graphics, int left, int y, GroupEntry entry, int mouseX, int mouseY) {
        boolean selected = entry.id().equals(selectedGroupId);
        boolean rowHovered = mouseX >= left + 10 && mouseX <= left + PANEL_WIDTH - 10 - LOCK_BUTTON_WIDTH
            && mouseY >= y && mouseY < y + ROW_HEIGHT && mouseY >= listTop && mouseY <= listBottom;

        if (selected) {
            graphics.fill(left + 8, y, left + PANEL_WIDTH - 8, y + ROW_HEIGHT - 2, 0x552ECC71);
        } else if (rowHovered) {
            graphics.fill(left + 8, y, left + PANEL_WIDTH - 8, y + ROW_HEIGHT - 2, 0x30FFFFFF);
        }

        int nameColor = selected ? 0x2ECC71 : 0xFFFFFF;
        graphics.drawString(this.font, entry.name(), left + 14, y + 3, nameColor, false);

        if (!entry.creatorName().isEmpty()) {
            graphics.drawString(this.font,
                Component.translatable("menu."+ LinkedHeaven.MODID+".by", entry.creatorName()),
                left + 14, y + 14, 0x999999, false);
        }

        boolean isGlobal = entry.name().equalsIgnoreCase("global") && entry.creatorName().isEmpty();
        if (!isGlobal) {
            renderLockIcon(graphics, left, y, entry, mouseX, mouseY);
        }
    }

    private void renderLockIcon(GuiGraphics graphics, int left, int y, GroupEntry entry, int mouseX, int mouseY) {
        int iconX = left + PANEL_WIDTH - 10 - LOCK_BUTTON_WIDTH;
        int iconY = y + 5;
        boolean hovered = entry.mine() && mouseX >= iconX && mouseX <= iconX + LOCK_BUTTON_WIDTH
            && mouseY >= y && mouseY < y + ROW_HEIGHT && mouseY >= listTop && mouseY <= listBottom;

        Component label = Component.translatable(entry.locked()
            ? "menu."+ LinkedHeaven.MODID+".locked"
            : "menu."+ LinkedHeaven.MODID+".unlocked");
        int color;
        if (!entry.mine()) {
            color = entry.locked() ? 0xAA6666 : 0x777777;
        } else {
            color = hovered ? 0xFFFFFF : (entry.locked() ? 0xFF6B6B : 0xAAFFAA);
        }
        graphics.drawString(this.font, label, iconX, iconY, color, false);
    }

    private int contentHeight() {
        return visibleRows.size() * ROW_HEIGHT;
    }

    private void renderScrollbar(GuiGraphics graphics, int left) {
        int viewport = listBottom - listTop;
        int content = contentHeight();
        int trackX = left + PANEL_WIDTH - 6;
        int barHeight = Math.max(12, viewport * viewport / content);
        int maxScroll = content - viewport;
        int barY = listTop + (maxScroll <= 0 ? 0 : (viewport - barHeight) * scrollOffset / maxScroll);
        graphics.fill(trackX, listTop, trackX + 3, listBottom, 0x40FFFFFF);
        graphics.fill(trackX, barY, trackX + 3, barY + barHeight, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= listTop && mouseY <= listBottom) {
            int left = (this.width - PANEL_WIDTH) / 2;
            int relativeY = (int) (mouseY - (listTop - scrollOffset));
            int index = relativeY / ROW_HEIGHT;
            if (index >= 0 && index < visibleRows.size()) {
                GroupEntry entry = visibleRows.get(index);
                int iconX = left + PANEL_WIDTH - 10 - LOCK_BUTTON_WIDTH;
                boolean isGlobal = entry.name().equalsIgnoreCase("global") && entry.creatorName().isEmpty();
                if (!isGlobal && entry.mine() && mouseX >= iconX) {
                    PacketDistributor.sendToServer(new SetGroupLockedPayload(entry.id(), !entry.locked()));
                    return true;
                }
                PacketDistributor.sendToServer(new SelectGroupPayload(entry.id()));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int viewport = listBottom - listTop;
        int maxScroll = Math.max(0, contentHeight() - viewport);
        if (maxScroll > 0 && mouseY >= listTop && mouseY <= listBottom) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (scrollY * ROW_HEIGHT / 2)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
