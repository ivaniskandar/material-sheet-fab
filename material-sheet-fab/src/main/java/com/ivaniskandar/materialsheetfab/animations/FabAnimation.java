package com.ivaniskandar.materialsheetfab.animations;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;

/**
 * Created by Gordon Wong on 7/9/2015.
 *
 * Animates the FAB when showing and hiding the material sheet.
 */
public class FabAnimation {

	protected View fab;
	protected Interpolator interpolator;

	public FabAnimation(View fab, Interpolator interpolator) {
		this.fab = fab;
		this.interpolator = interpolator;
	}

	/**
	 * Animates the FAB as if the FAB is morphing into a sheet.
	 *
	 * @param duration Duration of the animation in milliseconds. Use 0 for no animation.
	 * @param listener Listener for animation events.
	 */
	public void morphIntoSheet(long duration, final AnimationListener listener) {
		// Setup animation
		Animation fadeIn = new AlphaAnimation(1, 0);
		Animation scale = new ScaleAnimation(1f, 2f, 1f, 2f,
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
		fab.startAnimation(animationSet);
	}

	/**
	 * Animates the FAB as if a sheet is being morphed into a FAB.
	 *
	 * @param duration Duration of the animation in milliseconds. Use 0 for no animation.
	 * @param listener Listener for animation events.
	 */
	public void morphFromSheet(long duration, final AnimationListener listener) {
		fab.setVisibility(View.VISIBLE);
        // Setup animation
        Animation fadeIn = new AlphaAnimation(0, 1);
        Animation scale = new ScaleAnimation(2f, 1f, 2f, 1f,
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
        fab.startAnimation(animationSet);
	}
}
