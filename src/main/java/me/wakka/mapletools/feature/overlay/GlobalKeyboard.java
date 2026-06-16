package me.wakka.mapletools.feature.overlay;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;

public class GlobalKeyboard implements NativeKeyListener {

	private final OverlayWindow overlayWindow;

	public GlobalKeyboard(OverlayWindow overlayWindow) {
		this.overlayWindow = overlayWindow;
	}

	public void start() {
		try {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(this);
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			GlobalScreen.removeNativeKeyListener(this);
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent event) {
		double amount = (event.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 ? 10 : 1;

		Platform.runLater(() -> {
			switch (event.getKeyCode()) {
				case NativeKeyEvent.VC_UP -> overlayWindow.moveSelectedRegion(0, -amount);
				case NativeKeyEvent.VC_DOWN -> overlayWindow.moveSelectedRegion(0, amount);
				case NativeKeyEvent.VC_LEFT -> overlayWindow.moveSelectedRegion(-amount, 0);
				case NativeKeyEvent.VC_RIGHT -> overlayWindow.moveSelectedRegion(amount, 0);

				case NativeKeyEvent.VC_W -> overlayWindow.resizeSelectedRegion(0, -amount);
				case NativeKeyEvent.VC_S -> overlayWindow.resizeSelectedRegion(0, amount);
				case NativeKeyEvent.VC_A -> overlayWindow.resizeSelectedRegion(-amount, 0);
				case NativeKeyEvent.VC_D -> overlayWindow.resizeSelectedRegion(amount, 0);
			}
		});
	}
}
