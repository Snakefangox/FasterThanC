package net.snakefangox.fasterthanc.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.snakefangox.fasterthanc.gui.parts.WBlankPanel;
import net.snakefangox.fasterthanc.gui.parts.WFireButton;
import net.snakefangox.fasterthanc.gui.parts.WTargetingTextBox;
import spinnery.client.screen.BaseContainerScreen;
import spinnery.client.screen.BaseHandledScreen;
import spinnery.widget.*;
import spinnery.widget.api.Position;
import spinnery.widget.api.Size;

public class TargetingComputerScreen extends BaseHandledScreen<TargetingComputerContainer> {

	WVerticalScrollableContainer energyNetScroll;
	TargetingComputerContainer linkedContainer;

	public TargetingComputerScreen(TargetingComputerContainer handler, PlayerInventory inventory, Text title) {
		super(title, handler, inventory.player);
		this.linkedContainer = handler;
		WInterface wInterface = getInterface();
		wInterface.setTheme("spinnery:dark");
		WPanel mainPanel = wInterface.createChild(WPanel::new, Position.of(0, 0, 0),
				Size.of(9 * 18 + 8, 3 * 18 + 148)).setParent(wInterface);
		mainPanel.setOnAlign(WAbstractWidget::center);
		mainPanel.center();
		wInterface.add(mainPanel);
		WSlot.addPlayerInventory(Position.of(mainPanel, ((mainPanel.getWidth()) / 2) - (int) (18 * 4.5f), 3 * 18 + 64, 1),
				Size.of(18, 18), wInterface);
		energyNetScroll = mainPanel.createChild(WVerticalScrollableContainer::new, Position.of(mainPanel, 5, 5, 0),
				Size.of(mainPanel.getWidth() - 10, (mainPanel.getHeight() / 2) - 2));
		energyNetScroll.onAlign();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, tickDelta);
		if (linkedContainer.shouldUpdate && linkedContainer.uuids.length > 0) {
			linkedContainer.shouldUpdate = false;
			float offset = energyNetScroll.getOffsetY();
			energyNetScroll.remove(energyNetScroll.getWidgets().toArray(new WAbstractWidget[energyNetScroll.getWidgets().size()]));

			WBlankPanel fireAllPanel = energyNetScroll.createChild(WBlankPanel::new, Position.of(energyNetScroll, 0, 16),
					Size.of(energyNetScroll.getWidth() - energyNetScroll.getScrollbarWidth() - 8, 16));
			fireAllPanel.createChild(WStaticText::new, Position.of(fireAllPanel, 20, 4, 10), Size.of(100, 16))
					.setText(new TranslatableText("text.fasterthanc.fire_all"));
			fireAllPanel.createChild(WFireButton::new, Position.of(fireAllPanel), Size.of(16, 16)).setId(-1 - linkedContainer.uuids.length)
					.setPos(linkedContainer.controllerBE.getPos());
			energyNetScroll.addRow(fireAllPanel);

			for (int j = 0; j < linkedContainer.uuids.length; j++) {
				WBlankPanel panel = energyNetScroll.createChild(WBlankPanel::new, Position.of(energyNetScroll, 0, 16 * (j + 1)),
						Size.of(energyNetScroll.getWidth() - energyNetScroll.getScrollbarWidth() - 8, 30));
				panel.createChild(WStaticText::new, Position.of(panel, 20, 2, 10), Size.of(100, 16))
						.setText(linkedContainer.names[j]);
				panel.createChild(WFireButton::new, Position.of(panel), Size.of(16, 16)).setId(j)
						.setPos(linkedContainer.controllerBE.getPos());
				panel.createChild(WStaticText::new, Position.of(panel, 20, 18, 10), Size.of(100, 8))
						.setText(Formatting.GRAY + linkedContainer.uuids[j].toString().split("-", 2)[0]);
				WTargetingTextBox pitchBox = panel.createChild(WTargetingTextBox::new, Position.of(panel, 75, 12), Size.of(30, 18))
						.setId(j).setPos(linkedContainer.controllerBE.getPos()).setPitch(true)
						.setText(String.valueOf(linkedContainer.pitch[j]));
				WTargetingTextBox yawBox = panel.createChild(WTargetingTextBox::new, Position.of(panel, 110, 12), Size.of(30, 18))
						.setId(j).setPos(linkedContainer.controllerBE.getPos()).setPitch(false)
						.setText(String.valueOf(linkedContainer.yaw[j]));
				pitchBox.setTwin(yawBox);
				yawBox.setTwin(pitchBox);
				energyNetScroll.addRow(panel);
			}
			energyNetScroll.setOffsetY(offset - 0.2f);
			energyNetScroll.scroll(0,0.2);
		}
	}
}
