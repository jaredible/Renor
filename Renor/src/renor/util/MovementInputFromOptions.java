package renor.util;

import renor.misc.GameSettings;

public class MovementInputFromOptions extends MovementInput {
	private GameSettings gameSettings;

	public MovementInputFromOptions(GameSettings gameSettings) {
		this.gameSettings = gameSettings;
	}

	public void updatePlayerMoveState() {
		moveForward = 0.0f;
		moveStrafe = 0.0f;

		if (gameSettings.keyBindForward.getIsKeyPressed()) ++moveForward;
		if (gameSettings.keyBindBack.getIsKeyPressed()) --moveForward;
		if (gameSettings.keyBindLeft.getIsKeyPressed()) ++moveStrafe;
		if (gameSettings.keyBindRight.getIsKeyPressed()) --moveStrafe;

		jump = gameSettings.keyBindJump.getIsKeyPressed();
		sneak = gameSettings.keyBindSneak.getIsKeyPressed();

		if (sneak) {
			moveStrafe = (float) ((double) moveStrafe * 0.3);
			moveForward = (float) ((double) moveForward * 0.3);
		}
	}
}
