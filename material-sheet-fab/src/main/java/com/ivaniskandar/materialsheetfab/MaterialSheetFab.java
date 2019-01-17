package com.ivaniskandar.materialsheetfab;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.ivaniskandar.materialsheetfab.animations.AnimationListener;
import com.ivaniskandar.materialsheetfab.animations.FabAnimation;
import com.ivaniskandar.materialsheetfab.animations.MaterialSheetAnimation;
import com.ivaniskandar.materialsheetfab.animations.OverlayAnimation;

/**
 * Created by Gordon Wong on 7/9/2015.
 * 
 * Handles the interactions between the FAB and the material sheet that the FAB morphs into.
 */
public class MaterialSheetFab<FAB extends View & AnimatedFab> {

	private static final int ANIMATION_SPEED = 1;

	// Animation durations
	private static final int SHEET_ANIM_DURATION = 300 * ANIMATION_SPEED;
	private static final int SHOW_SHEET_COLOR_ANIM_DURATION = (int) (SHEET_ANIM_DURATION * 0.75);
	private static final int HIDE_SHEET_COLOR_ANIM_DURATION = (int) (SHEET_ANIM_DURATION * 0.75);
	private static final int FAB_ANIM_DURATION = 300 * ANIMATION_SPEED;
	private static final int SHOW_OVERLAY_ANIM_DURATION = MaterialSheetFab.SHOW_SHEET_ANIM_DELAY
			+ SHEET_ANIM_DURATION;
	private static final int HIDE_OVERLAY_ANIM_DURATION = SHEET_ANIM_DURATION;

	// Animation delays
	private static final int SHOW_SHEET_ANIM_DELAY = 0;
	private static final int MOVE_FAB_ANIM_DELAY = 0;

	// Views
	protected FAB fab;

	// Animations
	protected FabAnimation fabAnimation;
	protected MaterialSheetAnimation sheetAnimation;
	protected OverlayAnimation overlayAnimation;

	// State
	protected int anchorX;
	protected int anchorY;
	private boolean isShowing;
	private boolean isHiding;
	private boolean hideSheetAfterSheetIsShown;

	// Listeners
	private MaterialSheetFabEventListener eventListener;

	public enum RevealXDirection {
		LEFT, RIGHT
	}

	public enum RevealYDirection {
		UP, DOWN
	}

	/**
	 * Creates a MaterialSheetFab instance and sets up the necessary click listeners.
	 *
	 * @param fab The FAB view.
	 * @param sheet The sheet view.
	 * @param overlay The overlay view.
	 * @param sheetColor The background color of the material sheet.
	 * @param fabColor The background color of the FAB.
	 */
	public MaterialSheetFab(FAB fab, View sheet, View overlay, int sheetColor, int fabColor) {
		Interpolator interpolator = AnimationUtils.loadInterpolator(sheet.getContext(),
				R.interpolator.msf_interpolator);

		this.fab = fab;

		// Create animations
		fabAnimation = new FabAnimation(fab, interpolator);
		sheetAnimation = new MaterialSheetAnimation(sheet, sheetColor, fabColor, interpolator);
		overlayAnimation = new OverlayAnimation(overlay, interpolator);

		// Set initial visibilities
		sheet.setVisibility(View.INVISIBLE);
		overlay.setVisibility(View.GONE);

		// Set listener to morph FAB into sheet when clicked
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showSheet();
			}
		});

		// Set listener to hide the sheet when touching the overlay
		overlay.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				// Only hide if the sheet is visible and if this is the first touch event
				if (isSheetVisible() && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					hideSheet();
				}
				return true;
			}
		});

		// Set listener for when FAB view is laid out
		fab.getViewTreeObserver()
				.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						// Remove listener so that this is only called once
						MaterialSheetFab.this.fab.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						// Initialize FAB anchor when the FAB view is laid out
						updateFabAnchor();
					}
				});
	}

	/**
	 * Shows the FAB.
	 */
	public void showFab() {
		showFab(0, 0);
	}

	/**
	 * Shows the FAB and sets the FAB's translation.
	 *
	 * @param translationX translation X value
	 * @param translationY translation Y value
	 */
	public void showFab(float translationX, float translationY) {
		// Update FAB's anchor
		setFabAnchor(translationX, translationY);
		// Show the FAB in the new position if the sheet is not visible
		if (!isSheetVisible()) {
			fab.show(translationX, translationY);
		}
	}

	/**
	 * Shows the sheet.
	 */
	public void showSheet() {
		if (isAnimating()) {
			return;
		}
		isShowing = true;

		// Show overlay
		overlayAnimation.show(SHOW_OVERLAY_ANIM_DURATION, null);

		// Morph FAB into sheet
		morphIntoSheet(new AnimationListener() {
			@Override
			public void onEnd() {
				// Call event listener
				if (eventListener != null) {
					eventListener.onSheetShown();
				}

				// Assuming that this is the last animation to finish
				isShowing = false;

				// Hide sheet after it is shown
				if (hideSheetAfterSheetIsShown) {
					hideSheet();
					hideSheetAfterSheetIsShown = false;
				}
			}
		});

		// Call event listener
		if (eventListener != null) {
			eventListener.onShowSheet();
		}
	}

	/**
	 * Hides the sheet.
	 */
	public void hideSheet() {
		hideSheet(null);
	}

	protected void hideSheet(final AnimationListener endListener) {
		if (isAnimating()) {
			// Wait until the sheet is shown and then hide it
			if (isShowing) {
				hideSheetAfterSheetIsShown = true;
			}
			return;
		}
		isHiding = true;

		// Hide overlay
		overlayAnimation.hide(HIDE_OVERLAY_ANIM_DURATION, null);

		// Morph FAB from sheet
		morphFromSheet(new AnimationListener() {
			@Override
			public void onEnd() {
				// Call event listeners
				if (endListener != null) {
					endListener.onEnd();
				}
				if (eventListener != null) {
					eventListener.onSheetHidden();
				}

				// Assuming that this is the last animation to finish
				isHiding = false;
			}
		});

		// Call event listener
		if (eventListener != null) {
			eventListener.onHideSheet();
		}
	}

	/**
	 * Hides the sheet (if visible) and then hides the FAB.
	 */
	public void hideSheetThenFab() {
		AnimationListener listener = new AnimationListener() {
			@Override
			public void onEnd() {
				fab.hide();
			}
		};
		// Hide sheet then hide FAB
		if (isSheetVisible()) {
			hideSheet(listener);
		}
		// Hide FAB
		else {
			listener.onEnd();
		}
	}

	protected void morphIntoSheet(final AnimationListener endListener) {

		// Update FAB anchor to ensure that the FAB returns to the correct position when hiding the
		// sheet
		updateFabAnchor();

		// Align sheet's position with FAB
		sheetAnimation.alignSheetWithFab(fab);

		// Morph FAB into sheet
		fabAnimation.morphIntoSheet(FAB_ANIM_DURATION, null);

		// Show sheet after a delay
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// Hide FAB
				fab.setVisibility(View.INVISIBLE);

				// Show sheet
				sheetAnimation.morphFromFab(fab, SHEET_ANIM_DURATION,
						SHOW_SHEET_COLOR_ANIM_DURATION, endListener);
			}
		}, SHOW_SHEET_ANIM_DELAY);
	}

	protected void morphFromSheet(final AnimationListener endListener) {
		// Morph sheet into FAB
		sheetAnimation.morphIntoFab(fab, SHEET_ANIM_DURATION, HIDE_SHEET_COLOR_ANIM_DURATION, null);

		// Show FAB after a delay
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// Hide sheet
				sheetAnimation.setSheetVisibility(View.INVISIBLE);

				// Show FAB
				fabAnimation.morphFromSheet(FAB_ANIM_DURATION, endListener);
			}
		}, MOVE_FAB_ANIM_DELAY);
	}

	protected void updateFabAnchor() {
		// Update the anchor with the current translation
		setFabAnchor(fab.getTranslationX(), fab.getTranslationY());
	}

	protected void setFabAnchor(float translationX, float translationY) {
		anchorX = Math
				.round(fab.getX() + (fab.getWidth() / 2) + (translationX - fab.getTranslationX()));
		anchorY = Math
				.round(fab.getY() + (fab.getHeight() / 2) + (translationY - fab.getTranslationY()));
	}

	private boolean isAnimating() {
		return isShowing || isHiding;
	}

	public boolean isSheetVisible() {
		return sheetAnimation.isSheetVisible();
	}

	public void setEventListener(MaterialSheetFabEventListener eventListener) {
		this.eventListener = eventListener;
	}

}
