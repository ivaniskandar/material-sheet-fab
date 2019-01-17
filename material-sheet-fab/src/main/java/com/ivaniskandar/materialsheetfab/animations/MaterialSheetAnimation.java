package com.ivaniskandar.materialsheetfab.animations;

import java.lang.reflect.Method;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;

import com.ivaniskandar.materialsheetfab.MaterialSheetFab.RevealXDirection;
import com.ivaniskandar.materialsheetfab.MaterialSheetFab.RevealYDirection;

/**
 * Created by Gordon Wong on 7/5/2015.
 *
 * Animates the material sheet into and out of view.
 */
public class MaterialSheetAnimation {

	private static final String SUPPORT_CARDVIEW_CLASSNAME = "android.support.v7.widget.CardView";
	private static final int SHEET_REVEAL_OFFSET_Y = 0;

	private View sheet;
	private int sheetColor;
	private int fabColor;
	private Interpolator interpolator;
	private RevealXDirection revealXDirection;
	private RevealYDirection revealYDirection;
	private Method setCardBackgroundColor;
	private boolean isSupportCardView;

	public MaterialSheetAnimation(View sheet, int sheetColor, int fabColor,
			Interpolator interpolator) {
		this.sheet = sheet;
		this.sheetColor = sheetColor;
		this.fabColor = fabColor;
		this.interpolator = interpolator;
		// Default reveal direction is up and to the left (for FABs in the bottom right corner)
		revealXDirection = RevealXDirection.LEFT;
		revealYDirection = RevealYDirection.UP;
		isSupportCardView = sheet.getClass().getName().equals(SUPPORT_CARDVIEW_CLASSNAME);
		// Get setCardBackgroundColor() method
		if (isSupportCardView) {
			try {
				// noinspection unchecked
				setCardBackgroundColor = sheet.getClass()
						.getDeclaredMethod("setCardBackgroundColor", int.class);
			} catch (Exception e) {
				setCardBackgroundColor = null;
			}
		}
	}

	/**
	 * Aligns the sheet's position with the FAB.
	 * 
	 * @param fab Floating action button
	 */
	public void alignSheetWithFab(View fab) {
		// NOTE: View.getLocationOnScreen() returns the view's coordinates on the screen
		// whereas other view methods like getRight() and getY() return coordinates relative
		// to the view's parent. Using those methods can lead to incorrect calculations when
		// the two views do not have the same parent.

		// Get FAB's coordinates
		int[] fabCoords = new int[2];
		fab.getLocationOnScreen(fabCoords);

		// Get sheet's coordinates
		int[] sheetCoords = new int[2];
		sheet.getLocationOnScreen(sheetCoords);

		// NOTE: Use the diffs between the positions of the FAB and sheet to align the sheet.
		// We have to use the diffs because the coordinates returned by getLocationOnScreen()
		// include the status bar and any other system UI elements, meaning the coordinates
		// aren't representative of the usable screen space.
		int leftDiff = sheetCoords[0] - fabCoords[0];
		int rightDiff = (sheetCoords[0] + sheet.getWidth()) - (fabCoords[0] + fab.getWidth());
		int topDiff = sheetCoords[1] - fabCoords[1];
		int bottomDiff = (sheetCoords[1] + sheet.getHeight()) - (fabCoords[1] + fab.getHeight());

		// NOTE: Preserve the sheet's margins to allow users to shift the sheet's position
		// to compensate for the fact that the design support library's FAB has extra
		// padding within the view
		ViewGroup.MarginLayoutParams sheetLayoutParams = (ViewGroup.MarginLayoutParams) sheet
				.getLayoutParams();

		// Set sheet's new coordinates (only if there is a change in coordinates because
		// setting the same coordinates can cause the view to "drift" - moving 0.5 to 1 pixels
		// around the screen)
		if (rightDiff != 0) {
			float sheetX = sheet.getX();
			// Align the right side of the sheet with the right side of the FAB if
			// doing so would not move the sheet off the screen
			if (rightDiff <= sheetX) {
				sheet.setX(sheetX - rightDiff - sheetLayoutParams.rightMargin);
				revealXDirection = RevealXDirection.LEFT;
			}
			// Otherwise, align the left side of the sheet with the left side of the FAB
			else if (leftDiff != 0 && leftDiff <= sheetX) {
				sheet.setX(sheetX - leftDiff + sheetLayoutParams.leftMargin);
				revealXDirection = RevealXDirection.RIGHT;
			}
		}

		if (bottomDiff != 0) {
			float sheetY = sheet.getY();
			// Align the bottom of the sheet with the bottom of the FAB
			if (bottomDiff <= sheetY) {
				sheet.setY(sheetY - bottomDiff - sheetLayoutParams.bottomMargin);
				revealYDirection = RevealYDirection.UP;
			}
			// Otherwise, align the top of the sheet with the top of the FAB
			else if (topDiff != 0 && topDiff <= sheetY) {
				sheet.setY(sheetY - topDiff + sheetLayoutParams.topMargin);
				revealYDirection = RevealYDirection.DOWN;
			}
		}
	}

	/**
	 * Shows the sheet by morphing the FAB into the sheet.
	 *
	 * @param fab Floating action button
	 * @param showSheetDuration Duration of the sheet animation in milliseconds. Use 0 for no
	 *            animation.
	 * @param showSheetColorDuration Duration of the color animation in milliseconds. Use 0 for no
	 *            animation.
	 * @param listener Listener for animation events.
	 */
	public void morphFromFab(View fab, long showSheetDuration, long showSheetColorDuration,
			AnimationListener listener) {
		sheet.setVisibility(View.VISIBLE);
		if (listener != null) {
			listener.onStart();
		}

		// Pass listener to the animation that will be the last to finish
		AnimationListener revealListener = (showSheetDuration >= showSheetColorDuration) ? listener : null;
		AnimationListener colorListener = (showSheetColorDuration > showSheetDuration) ? listener : null;

		startExpandAnimation(sheet, showSheetDuration, interpolator, revealListener);
		startColorAnim(sheet, fabColor, sheetColor, showSheetColorDuration, interpolator,
				colorListener);
	}

	/**
	 * Hides the sheet by morphing the sheet into the FAB.
	 *
	 * @param fab Floating action button
	 * @param hideSheetDuration Duration of the sheet animation in milliseconds. Use 0 for no
	 *            animation.
	 * @param hideSheetColorDuration Duration of the color animation in milliseconds. Use 0 for no
	 *            animation.
	 * @param listener Listener for animation events.
	 */
	public void morphIntoFab(View fab, long hideSheetDuration, long hideSheetColorDuration,
			AnimationListener listener) {
		if (listener != null) {
			listener.onStart();
		}

		// Pass listener to the animation that will be the last to finish
		AnimationListener revealListener = (hideSheetDuration >= hideSheetColorDuration) ? listener : null;
		AnimationListener colorListener = (hideSheetColorDuration > hideSheetDuration) ? listener : null;

		startShrinkAnimation(sheet, hideSheetDuration, interpolator, revealListener);
//		startColorAnim(sheet, fabColor, sheetColor, hideSheetColorDuration, interpolator,
//				colorListener);
	}

	protected void startExpandAnimation(View view, long duration, Interpolator interpolator,
										final AnimationListener listener) {
		// Setup animation
        Animation fadeIn = new AlphaAnimation(0, 1);
		Animation scale = new ScaleAnimation(0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 1);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(scale);
        animationSet.setDuration(duration);
        animationSet.setInterpolator(interpolator);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (listener != null) {
                    listener.onStart();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (listener != null) {
                    listener.onEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Start animation
		view.startAnimation(animationSet);
	}

	protected void startShrinkAnimation(View view, long duration, Interpolator interpolator,
										final AnimationListener listener) {
        // Setup animation
        Animation fadeIn = new AlphaAnimation(1, 0);
        Animation scale = new ScaleAnimation(1f, 0f, 1f, 0f,
                Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 1);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(scale);
        animationSet.setDuration(duration);
        animationSet.setInterpolator(interpolator);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (listener != null) {
                    listener.onStart();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (listener != null) {
                    listener.onEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Start animation
        view.startAnimation(animationSet);
	}


	protected void startColorAnim(final View view, final int startColor, final int endColor,
			long duration, Interpolator interpolator, final AnimationListener listener) {
		// Setup animation
		ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
		anim.setDuration(duration);
		anim.setInterpolator(interpolator);
		// Add listeners
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				if (listener != null) {
					listener.onStart();
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (listener != null) {
					listener.onEnd();
				}
			}
		});
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				// Update background color
				Integer color = (Integer) animator.getAnimatedValue();

				// Use CardView.setCardBackgroundColor() to avoid crashes on Android < 5.0 and to
				// properly set the card's background color without removing the card's other styles
				// See https://github.com/gowong/material-sheet-fab/pull/2 and
				// https://code.google.com/p/android/issues/detail?id=77843
				if (isSupportCardView) {
					// Use setCardBackground() method if it is available
					if (setCardBackgroundColor != null) {
						try {
							setCardBackgroundColor.invoke(sheet, color);
						} catch (Exception e) {
							// Ignore exceptions since there's no other way set a support CardView's
							// background color
						}
					}
				}
				// Set background color for all other views
				else {
					view.setBackgroundColor(color);
				}
			}
		});
		// Start animation
		anim.start();
	}

	public void setSheetVisibility(int visibility) {
		sheet.setVisibility(visibility);
	}

	public boolean isSheetVisible() {
		return sheet.getVisibility() == View.VISIBLE;
	}

	protected float getSheetRevealRadius() {
		return Math.max(sheet.getWidth(), sheet.getHeight());
	}

	protected float getFabRevealRadius(View fab) {
		return Math.max(fab.getWidth(), fab.getHeight()) / 2;
	}

	public RevealXDirection getRevealXDirection() {
		return revealXDirection;
	}

	public RevealYDirection getRevealYDirection() {
		return revealYDirection;
	}
}
