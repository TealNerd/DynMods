package com.biggestnerd.dynmods.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import com.biggestnerd.dynmods.DynMod;
import com.biggestnerd.dynmods.DynMods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;

public class GuiModList extends GuiScreen {

	private final GuiScreen parent;
	private ArrayList<DynMod> mods;
	private int selected = -1;
	private GuiButton enableButton;
	private GuiButton disableButton;
	private ModList container;
	
	public GuiModList(GuiScreen parent) {
		this.parent = parent;
	}
	
	public void initGui() {
		buttonList.clear();
		buttonList.add(enableButton = new GuiButton(0, this.width / 2 - 100, this.height - 53, 98, 20, "Enable"));
		buttonList.add(disableButton = new GuiButton(1, this.width / 2 + 2, this.height - 53, 98, 20, "Disable"));
		buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height - 32, "Reload"));
		container = new ModList(mc);
		container.registerScrollButtons(4, 5);
		enableButton.enabled = false;
		disableButton.enabled = false;
		mods = DynMods.getModList();
		if(mods.size() >= 0) {
			container.elementClicked(0, false, 1, 1);
		}
	}
	
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		container.handleMouseInput();
	}
	
	public void actionPerformed(GuiButton button) throws IOException {
		if(button.enabled) {
			if(button.id == 2) {
				DynMods.refreshModList();
				mods = DynMods.getModList();
				return;
			}
			DynMod mod = mods.get(selected);
			if(mod != null) {
				if(button.id == 0) {
					mod.setEnabled(true);
				} else {
					mod.setEnabled(false);
				}
			}
		}
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		container.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(fontRendererObj, "Mod List", this.width / 2, 20, Color.WHITE.getRGB());
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	class ModList extends GuiSlot {
		
		public ModList(Minecraft mc) {
			super(mc, GuiModList.this.width, GuiModList.this.height, 32, GuiModList.this.height - 64, 36);
		}

		@Override
		protected int getSize() {
			return GuiModList.this.mods.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			GuiModList.this.selected = slotIndex;
			boolean valid = slotIndex >= 0 && slotIndex < getSize();
			GuiModList.this.enableButton.enabled = valid;
			GuiModList.this.disableButton.enabled = valid;
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == GuiModList.this.selected;
		}
		
		@Override
		public int getContentHeight() {
			return getSize() * mc.fontRendererObj.FONT_HEIGHT;
		}

		@Override
		protected void drawBackground() {
			GuiModList.this.drawDefaultBackground();
		}

		@Override
		protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn) {
			DynMod mod = GuiModList.this.mods.get(entryID);
			GuiModList.this.drawString(mc.fontRendererObj, mod.getName() + ": ", insideLeft + 1, yPos + 1, Color.WHITE.getRGB());
			int width = mc.fontRendererObj.getStringWidth(mod.getName());
			if(mod.isEnabled()) {
				GuiModList.this.drawString(mc.fontRendererObj, "Enabled", insideLeft + width + 5, yPos + 1, Color.GREEN.getRGB());
			} else {
				GuiModList.this.drawString(mc.fontRendererObj, "Disabled", insideLeft + width + 5, yPos + 1, Color.RED.getRGB());
			}
		}
	}
}
