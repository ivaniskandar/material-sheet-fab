# MaterialSheetFab
[![](https://jitpack.io/v/ivaniskandar/material-sheet-fab.svg)](https://jitpack.io/#ivaniskandar/material-sheet-fab)

Library that mimics the [floating action button (FAB) menu](https://www.google.com/design/spec/components/buttons-floating-action-button.html#buttons-floating-action-button-transitions) from Google's Material Design documentation. It can be used with any FAB library on Android 4.0+ (API levels >= 14).

![Transition](art/lite.gif)

## Installation
### Gradle
Add the JitPack repository to your root `build.gradle`:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your project `build.gradle`:
```groovy
dependencies {
    implementation 'com.github.ivaniskandar:material-sheet-fab:1.0.0'
}
```

## Usage
### Implement the FAB:  
You can use any FAB library as long as it implements the `AnimatedFab` interface.  
```java
import android.support.design.widget.FloatingActionButton;

public class Fab extends FloatingActionButton implements AnimatedFab {

   /**
    * Shows the FAB.
    */
    @Override
    public void show() {
        show(0, 0);
    }

    /**
     * Shows the FAB and sets the FAB's translation.
     *
     * @param translationX translation X value
     * @param translationY translation Y value
     */
    @Override
    public void show(float translationX, float translationY) {
        // NOTE: Using the parameters is only needed if you want
        // to support moving the FAB around the screen.
        // NOTE: This immediately hides the FAB. An animation can 
        // be used instead - see the sample app.
        setVisibility(View.VISIBLE);
    }

    /**
     * Hides the FAB.
     */
    @Override
    public void hide() {
        // NOTE: This immediately hides the FAB. An animation can
        // be used instead - see the sample app.
        setVisibility(View.INVISIBLE);
    }
}
```

### Modify the layouts:  
```xml
<RelativeLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

   <!-- Your FAB implementation -->
   <path.to.your.fab
        android:id="@+id/fab"
        android:layout_alignParentBottom="true"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true" />

    <!-- Overlay that dims the screen -->
    <com.ivaniskandar.materialsheetfab.DimOverlayFrameLayout
        android:id="@+id/overlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Sheet that contains your items -->
    <android.support.v7.widget.CardView
        android:id="@+id/fab_sheet"
        android:layout_width="250dp"
        android:layout_height="300dp"
        android:layout_alignParentBottom="true"
        android:layout_alignParentEnd="true"
        android:layout_alignParentRight="true"
        android:layout_margin="16dp">

        <!-- TODO: Put your sheet items here -->

    </android.support.v7.widget.CardView>

</RelativeLayout>
```

### Initialize the MaterialSheetFab:  
This can be in your Activity or Fragment.  
```java
public class MaterialSheetFabActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fab fab = (Fab) findViewById(R.id.fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.dim_overlay);
        int sheetColor = getResources().getColor(R.color.fab_sheet_color);
        int fabColor = getResources().getColor(R.color.fab_color);

        // Initialize material sheet FAB
        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay,
            sheetColor, fabColor);
    }
}
```

### Optional:
**Close sheet on back button:**  
```java
@Override
public void onBackPressed() {
    if (materialSheetFab.isSheetVisible()) {
        materialSheetFab.hideSheet();
    } else {
        super.onBackPressed();
    }
}
```

**Listen to events:**  
```java
materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
    @Override
    public void onShowSheet() {
        // Called when the material sheet's "show" animation starts.
    }
    
    @Override
    public void onSheetShown() {
        // Called when the material sheet's "show" animation ends.
    }

    @Override
    public void onHideSheet() {
        // Called when the material sheet's "hide" animation starts.
    }
     
    public void onSheetHidden() {
        // Called when the material sheet's "hide" animation ends.
    }
});
```

**Move the FAB around the screen** (this is useful for coordinating with [snackbars](https://material.io/design/components/snackbars.html)):
```java
materialSheetFab.showFab(translationX, translationY);
```

## Changelog
See changelog [here](./CHANGELOG.md).

## Credits
[MaterialSheetFab](https://github.com/gowong/material-sheet-fab) library by [Gordon Wong](https://github.com/gowong) is used as the base of this project.

## License
```
The MIT License (MIT)

Copyright (c) 2015 Gordon Wong
Copyright (c) 2019 Ivan Iskandar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
