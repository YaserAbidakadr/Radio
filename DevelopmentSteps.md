# Radio Simultion

This project will use the Google ARCore featrue to simualtion the Radio propagation. The example rule is just like this

![Examples Result](https://static1.squarespace.com/static/5727a25a1bbee088172a1d40/t/59514e65197aea1f384cb763/1498500722514/?format=1000w)


## How to use this simulation
1.  Use the cell phone to walk around
2.  Scan a picture, the APP will show a 3D radio source in the scent.
3.  Click on the 3D source, the APP will calculate the signal


# Following the links to create a projectPlanned

-   [Google Codelab](https://codelabs.developers.google.com/codelabs/sceneform-intro/index.html?index=..%2F..%2Fio2018#0)

-   [Completed code](https://github.com/googlecodelabs/sceneform-intro)
# Create new project

# Upate Gradle scripts

## Enable Java 8

Sceneform uses language constructs from Java 8. For projects that have a min API level less than 26, you need to explicitly add support for Java 8.

In the android {} section of app/build.gradle add:
```XML
compileOptions {
   sourceCompatibility JavaVersion.VERSION_1_8
   targetCompatibility JavaVersion.VERSION_1_8
}
```

## Add the ARCore and Sceneform dependencies

In the app/build.gradle file add dependencies for the Sceneform API and the Sceneform UX elements.

In the dependency {} section add:
```XML
implementation "com.google.ar.sceneform.ux:sceneform-ux:1.0.0"
```

## Press the "Sync now" link to update the project.

# Add the Sceneform fragment

There are several aspects of making a great AR experience that involve interacting with multiple views. Things like displaying a non-text indicator that the user should move the phone in order for ARCore to detect planes and handling gestures for moving and scaling objects. To do this add ArFragment to the **app/res/layout/content_main.xml** file.

Open content_main.xml and let's add the fragment and the view. Here's the text of the layout file, but feel free to use the graphical view if that is more comfortable for you.

Replace the existing TextView element with the fragment:
```XML
    <fragment
   android:id="@+id/sceneform_fragment"
   android:name="com.google.ar.sceneform.ux.ArFragment"
   android:layout_width="match_parent"
   android:layout_height="match_parent" />
```

## Add ARCore AndroidManifest entries

ARCore requires entries in the AndroidManifest. These declare that the camera will be used by the application, and that the AR features are used along with designating the application as requiring ARCore to run. This last metadata entry is used by the Play Store to filter out applications for users on devices that are not supported by ARCore.

Open app/manifests/AndroidManifest.xml and in the <manifest> section add these elements:
```
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.ar" android:required="true" />
```
Then add the metadata in the <application> section:
```
<meta-data android:name="com.google.ar.core" android:value="required" />
```

## Add the ArFragment field

We'll be referencing the fragment a lot as we work with the AR scene. To make things easier, open MainActivity and add a member variable at the top of the class:
```Java
private ArFragment fragment;
```

-   Remember to Import classes!

    Remember that all classes not in the current class's package need to be imported!
    In Android Studio you can do this by pressing ⌥-return on a Mac, or Alt + Enter on Windows.

-   Initialize it at the bottom of onCreate(). Since we're using a fragment, we need to use the fragment manager to find the fragment:
```Java
fragment = (ArFragment)
  getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
```
Great! Now we have the minimum amount of code to start using ARCore and make sure it works. Next, let's try it out!

# Add a pointer

This application presents a set of models that can be placed on the AR scene. We could use drag and drop to select one of the models and drag it onto the view. However, what seems to work best is to just look at where you want to place the model and tap it. This keeps your fingers out of the way so you can see better, and also makes it less cumbersome to hold the phone in the right place as dragging.

To do this, we'll add a pointer in the form of an overlay. The overlay will always be centered on the screen, and when we take a picture of the scene later, the pointer will not be in the image.

The View overlay needs a Drawable, so click Menu **File>New>Java Class** to make a new class named **PointerDrawable**. This extends Drawable, let's set the Superclass to android.graphics.drawable.Drawable.

Click on the red "light bulb" in the editor and select "Implement Methods". This will generate the placeholder methods.

Our pointer will have 2 states, enabled, which means an object can be dropped on the scene at that location, and disabled, when it can't.

We need 2 member variables:

-   Paint object for drawing.
-   Boolean flag for indicating enabled or disabled.

Add these fields at the top of the class.
```Java
private final Paint paint = new Paint();
private boolean enabled;
```

Add a getter and setter for enabled

```Java
public boolean isEnabled() {
 return enabled;
}

public void setEnabled(boolean enabled) {
 this.enabled = enabled;
}
```

Now implement the draw method. We'll draw a circle in green when enabled, and an X in gray when disabled.
```Java
@Override
public void draw(@NonNull Canvas canvas) {
 float cx = canvas.getWidth()/2;
 float cy = canvas.getHeight()/2;
 if (enabled) {
   paint.setColor(Color.GREEN);
   canvas.drawCircle(cx, cy, 10, paint);
 }else {
   paint.setColor(Color.GRAY);
   canvas.drawText("X", cx, cy, paint);
 }
}
```
That's sufficient for our purposes, we can ignore implementing the other methods.

# Control the pointer

Go back to MainActivity and let's initialize the pointer and add the code to enable and disable it based on the tracking state from ARCore, and if the user is looking at a plane detected by ARCore.

Add 3 member variables to MainActivity:

-   PointerDrawable pointer
-   boolean isTracking - indicating if ARCore is in tracking state.
-   boolean isHitting - indicating the user is looking at a plane. The method for figuring this out is called hitTest which is why it is called isHitting.

```Java
 private PointerDrawable pointer = new PointerDrawable();
 private boolean isTracking;
 private boolean isHitting;
```

At the bottom of onCreate() add a listener to the ArSceneView scene which will get called before processing every frame. In this listener we can make ARCore API calls and update the status of the pointer.

We'll use a lambda to first call the fragment's onUpdate method, then we'll call a new method in MainActivity called onUpdate.
```Java
fragment.getArSceneView().getScene().setOnUpdateListener(frameTime -> {
 fragment.onUpdate(frameTime);
 onUpdate();
});
```

## Implement onUpdate().

First, update the tracking state. If ARCore is not tracking, remove the pointer until tracking is restored.

Next, if ARCore is tracking, check for the gaze of the user hitting a plane detected by ARCore and enable the pointer accordingly.
```Java
private void onUpdate() {
 boolean trackingChanged = updateTracking();
 View contentView = findViewById(android.R.id.content);
 if (trackingChanged) {
   if (isTracking) {
     contentView.getOverlay().add(pointer);
   } else {
     contentView.getOverlay().remove(pointer);
   }
   contentView.invalidate();
 }

 if (isTracking) {
   boolean hitTestChanged = updateHitTest();
   if (hitTestChanged) {
     pointer.setEnabled(isHitting);
     contentView.invalidate();
   }
 }
}
```

updateTracking() uses ARCore's camera state and returns true if the tracking state has changed since last call.

private boolean updateTracking() {
 Frame frame = fragment.getArSceneView().getArFrame();
 boolean wasTracking = isTracking;
 isTracking = frame.getCamera().getTrackingState() == TrackingState.TRACKING;
 return isTracking != wasTracking;
}

updateHitTest() also uses ARCore to call Frame.hitTest(). As soon as any hit is detected, the method returns. We also need the center of the screen for this method, so add a helper method getScreenCenter() as well.
```Java
private boolean updateHitTest() {
 Frame frame = fragment.getArSceneView().getArFrame();
 android.graphics.Point pt = getScreenCenter();
 List<HitResult> hits;
 boolean wasHitting = isHitting;
 isHitting = false;
 if (frame != null) {
   hits = frame.hitTest(pt.x, pt.y);
   for (HitResult hit : hits) {
     Trackable trackable = hit.getTrackable();
     if ((trackable instanceof Plane &&
             ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
       isHitting = true;
       break;
     }
   }
 }
 return wasHitting != isHitting;
}

private android.graphics.Point getScreenCenter() {
 View vw = findViewById(android.R.id.content);
 return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
}
```
Great! Now we have the pointer implemented, let's try it out and make sure it works.

# Add sample 3D models to the project

Android Studio 3.1 supports a new folder type named "sampledata". This folder is used for design time sample data. For our purposes, we'll keep the source 3D assets in this directory. Files in sampledata are not added to the APK, but available in the editor. To make sceneform compatible models, we'll add the conversion tasks to gradle and add them to the assets directory so they are available at runtime.

Create the sample data directory by clicking on app in the project view and then right mouse click to find the menu item New > Sample Data Directory.

Download the sampledata.zip resources from GitHub. This contains 2 directories:

-   models - copy this to app/sampledata
-   Thumbnails - copy the files to app/src/main/drawable
-   Note: the original file is wrinng, Thumbnails - copy the files to app/src/main/res/drawable

## when add asset in the projects, error

-   after add to the project, I found it has problem
-   Choose keep broken,
-   open module gbuold.Gradle
-   change the 'src/main/assets/andy' to 'srcmain/assets/andy'
-   it works, then change it back, it works again

-   It seems the letter capital has problem

# Add Gallery of models

Now we'll add a simple list of models we can add to our augmented world. RecyclerViews are great for showing a scrolling list of items, that's a topic for another day, We'll just use a LinearLayout.

## Add the LinearLayout to the layout file

Open up **app/res/layout/content_main.xml** and directly below the <fragment> element, add the LinearLayout.

Set the attributes of the LinearLayout:

-   id: @+id/gallery_layout
-   layout_width: match_parent (we want to span the width of the device)
-   layout_height: 0dp (this causes the height to be calculated by the constraint layout)
-   orientation: horizontal (fill the layout left to right)

Add the layout constraints to keep it at the bottom of the screen.

-   layout_constraintBottom_toBottomOf: parent
-   layout_constraintTop_toBottomOf: @+id/sceneform_fragment
-   layout_constraintVertical_chainStyle: spread
-   layout_constraintVertical_weight: 1
```XML
    <LinearLayout
        android:id="@+id/gallery_layout"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:orientation="horizontal"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/sceneform_fragment"
        app:layout_constraintVertical_chainStyle="spread"
        app:layout_constraintVertical_weight="1"/>
```

## Adjust the fragment layout

Change the fragment layout to layout the fragment on the top part of the screen:

-   layout_height: 0dp (this causes the height to be calculated by the constraint layout)
-   layout_constraintTop_toTopOf: parent
-   layout_constraintBottom_toTopOf: @id/gallery_layout
-   layout_constraintVertical_chainStyle: spread
-   layout_constraintVertical_weight: 9

When it is updated, it will look like this:
```XML
    <fragment
        android:id="@+id/sceneform_fragment"
        android:name="com.google.ar.sceneform.ux.ArFragment"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/gallery_layout"
        app:layout_constraintVertical_chainStyle="spread"
        app:layout_constraintVertical_weight="9"/>
```

# Initialize the Gallery of models

Now we'll build the gallery. Each item in the gallery has a name, a uri to the sfb model in the assets directory, and a resource id of the thumbnail image of the model.

In MainActivity add a method at the end of the class name initializeGallery().

In there, first get the gallery layout view, then create the items, and add them to the gallery.

For each item, create an ImageView for the thumbnail, and add an onClickListener to handle adding the model to the scene.
```Java
private void initializeGallery() {
 LinearLayout gallery = findViewById(R.id.gallery_layout);

 ImageView andy = new ImageView(this);
 andy.setImageResource(R.drawable.droid_thumb);
 andy.setContentDescription("andy");
 andy.setOnClickListener(view ->{addObject(Uri.parse("andy.sfb"));});
 gallery.addView(andy);

 ImageView cabin = new ImageView(this);
 cabin.setImageResource(R.drawable.cabin_thumb);
 cabin.setContentDescription("cabin");
 cabin.setOnClickListener(view ->{addObject(Uri.parse("Cabin.sfb"));});
 gallery.addView(cabin);

 ImageView house = new ImageView(this);
 house.setImageResource(R.drawable.house_thumb);
 house.setContentDescription("house");
 house.setOnClickListener(view ->{addObject(Uri.parse("House.sfb"));});
 gallery.addView(house);

 ImageView igloo = new ImageView(this);
 igloo.setImageResource(R.drawable.igloo_thumb);
 igloo.setContentDescription("igloo");
 igloo.setOnClickListener(view ->{addObject(Uri.parse("igloo.sfb"));});
 gallery.addView(igloo);
}
```

## Add the addObject method

This method is called when one of the items in the gallery is clicked. It performs a hittest to determine where in the 3D world space the object should be placed, then calls a method placeObject to actually place the object.
```Java
private void addObject(Uri model) {
 Frame frame = fragment.getArSceneView().getArFrame();
 Point pt = getScreenCenter();
 List<HitResult> hits;
 if (frame != null) {
   hits = frame.hitTest(pt.x, pt.y);
   for (HitResult hit : hits) {
     Trackable trackable = hit.getTrackable();
     if ((trackable instanceof Plane &&
             ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
       placeObject(fragment, hit.createAnchor(), model);
       break;

     }
   }
 }
}
```

## Add placeObject method

placeObject() takes the ARCore anchor from the hitTest result and builds the Sceneform nodes. It starts the asynchronous loading of the 3D model using the ModelRenderable builder. This codelab uses small models, but larger models could take substantially longer to load.

Once the model is loaded as a Renderable, call addNodeToScene. If there was an error, show an alert dialog.
```Java
private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
 CompletableFuture<Void> renderableFuture =
         ModelRenderable.builder()
                 .setSource(fragment.getContext(), model)
                 .build()
                 .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                 .exceptionally((throwable -> {
                  AlertDialog.Builder builder = new AlertDialog.Builder(this);
                     builder.setMessage(throwable.getMessage())
                       .setTitle("Codelab error!");
                     AlertDialog dialog = builder.create();
                    dialog.show();
                   return null;
                 }));
}
```

## Add addNodeToScene

addNodeToScene() builds two nodes and attaches them to the ArSceneView's scene object.

The first node is of type AnchorNode. Anchor nodes are positioned based on the pose of an ARCore Anchor. As a result, they stay positioned in the sample place relative to the real world.

The second Node is a TransformableNode. We could use the base class type, Node for the placing the objects, but Node does not have the interaction functionality to handle moving, scaling and rotating based on user gestures.

Once the nodes are created and connected to each other, connect the AnchorNode to the Scene, and select the node so it has the focus for interactions.
```Java
private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
 AnchorNode anchorNode = new AnchorNode(anchor);
 TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
 node.setRenderable(renderable);
 node.setParent(anchorNode);
 fragment.getArSceneView().getScene().addChild(anchorNode);
 node.select();
}
```
Great work!! Now we just need to call initializeGallery from onCreate(), at the end of the method:
```Java
   initializeGallery();
```

# Capture a photo

Now let's add the photo capturing. This will change the floating action button to save an image of the ArSceneView into the photos directory and launch an intent to view it.

## Change float button to camera
First let's change the floating action button to be a camera instead of an envelope.

In app/res/layout/activity_main.xml

Find the floating action button and change the srcCompat to be the camera icon:

```java
app:srcCompat="@android:drawable/ic_menu_camera" />
```
We also need a specifying the paths our application will write to. This file needs to be an xml resource file. Add this by selecting app/res in the project view, then right mouse clicking and select New > Directory. Name the directory xml.

Select the xml directory and create a new XML file named: paths.xml.

Add the external path to the images:
```XML
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
   <external-path name="my_images" path="Pictures" />
</paths>
```
In AndroidManifest.xml, add the url provider. This is needed to securely pass the url of the photo we took to the photos app via an intent

Inside the <application> element add:
```XML
<provider
   android:name="android.support.v4.content.FileProvider"
   android:authorities="${applicationId}.ar.codelab.name.provider"
   android:exported="false"
   android:grantUriPermissions="true">
   <meta-data
       android:name="android.support.FILE_PROVIDER_PATHS"
       android:resource="@xml/paths"/>
</provider>
```

## Add permission for external storage

This has two parts, first add it to the manifest next to the CAMERA permission
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
Next, we have to request this. We extend the fragment so we can request additional permissions. Create a new class called WritingArFragment with a super class of com.google.ar.sceneform.ux.ArFragment

Add the write external storage permission as an additional permission. This will make the fragment prompt the user for consent when the application starts.
```Java
public class WritingArFragment extends ArFragment {
   @Override
   public String[] getAdditionalPermissions() {
       String[] additionalPermissions = super.getAdditionalPermissions();
       int permissionLength = additionalPermissions != null ? additionalPermissions.length : 0;
       String[] permissions = new String[permissionLength + 1];
       permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
       if (permissionLength > 0) {
           System.arraycopy(additionalPermissions, 0, permissions, 1, additionalPermissions.length);
       }
       return permissions;
   }
}
```
Then change the content_main.xml layout to use WritingArFragment instead.
```XML
<fragment
   android:id="@+id/sceneform_fragment"
   android:name="fully.qualified.class.name.WritingArFragment"
   android:layout_width="match_parent"
   android:layout_height="0dp"
   app:layout_constraintTop_toTopOf="parent"
   app:layout_constraintBottom_toTopOf="@id/gallery_layout"
   app:layout_constraintVertical_chainStyle="spread"
   app:layout_constraintVertical_weight="9" />
```

## Add generateFilename method

A unique file name is needed for each picture we take. The filename for the picture is generated using the standard pictures directory, and then an album name of Sceneform. Each image name is based on the time, so they won't overwrite each other. This path is also related to the paths.xml file we added previously.
```Java
private String generateFilename() {
 String date =
         new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
 return Environment.getExternalStoragePublicDirectory(
         Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
}
```

## Add saveBitmapToDisk method

saveBitmapToDisk() writes out the bitmap to the file.
```Java
private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

   File out = new File(filename);
   if (!out.getParentFile().exists()) {
       out.getParentFile().mkdirs();
   }
   try (FileOutputStream outputStream = new FileOutputStream(filename);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
       bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
       outputData.writeTo(outputStream);
       outputStream.flush();
       outputStream.close();
   } catch (IOException ex) {
       throw new IOException("Failed to save bitmap to disk", ex);
   }
}
```

## Add the takePhoto method

The method takePhoto() uses the PixelCopy API to capture a screenshot of the ArSceneView. It is asynchronous since it actually happens between frames. When the listener is called, the bitmap is saved to the disk, and then a snackbar is shown with an intent to open the image in the Pictures application.
```Java
private void takePhoto() {
   final String filename = generateFilename();
   ArSceneView view = fragment.getArSceneView();

   // Create a bitmap the size of the scene view.
   final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
           Bitmap.Config.ARGB_8888);

   // Create a handler thread to offload the processing of the image.
   final HandlerThread handlerThread = new HandlerThread("PixelCopier");
   handlerThread.start();
   // Make the request to copy.
   PixelCopy.request(view, bitmap, (copyResult) -> {
       if (copyResult == PixelCopy.SUCCESS) {
           try {
               saveBitmapToDisk(bitmap, filename);
           } catch (IOException e) {
               Toast toast = Toast.makeText(MainActivity.this, e.toString(),
                       Toast.LENGTH_LONG);
               toast.show();
               return;
           }
           Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                   "Photo saved", Snackbar.LENGTH_LONG);
           snackbar.setAction("Open in Photos", v -> {
               File photoFile = new File(filename);

               Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                       MainActivity.this.getPackageName() + ".ar.codelab.name.provider",
                       photoFile);
               Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
               intent.setDataAndType(photoURI, "image/*");
               intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
               startActivity(intent);

           });
           snackbar.show();
       } else {
           Toast toast = Toast.makeText(MainActivity.this,
                   "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
           toast.show();
       }
       handlerThread.quitSafely();
   }, new Handler(handlerThread.getLooper()));
}
```
The last step is to change the floating action button onClickListener to call takePhoto(). This is in onCreate(). When you are done, it should look like this:
```
fab.setOnClickListener(new View.OnClickListener() {
   @Override
   public void onClick(View view) {
       takePhoto();
   }
});
```
Well Done! Now try it out one more time and take a picture!


## Error and fixs

-   Change this line to reflect the real project name in **content_main.xml**
```XML
<fragment
        android:id="@+id/sceneform_fragment"
        android:name="ca.gc.crc.rnad.radiosimulation.WritingArFragment"
```

-   Optional
change the **.ar.codelab.name.provider**  to **.rnad.radiosimulation.name.provider**
in two Files
    -   takePhoto file photoURI
    -   **${applicationId}.rnad.radiosimulation.name.provider** in AndroidManifest.xml

```xml
<provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}.rnad.radiosimulation.name.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/paths"/>
        </provider>
```

# Summary

Well done working through this codelab! A quick re-cap of what was covered:

-   Using the UX fragment from Sceneform to handle common ARCore initialization tasks and plane discovery
-   Using the Sceneform Android Studio plugin to import models into the project and preview them.
-   Using TransformableNode to implement moving, scaling and rotating nodes
-   Using the gaze as a pointer to determine where in worldspace to place objects
-   Capturing a screen image and saving as a Picture, suitable for sharing.

## Other Resources

As you continue your ARCore exploration. Check out these other resources:

-   AR Concepts: https://developers.google.com/ar/discover/concepts
-   Google Developers ARCore https://developers.google.com/ar/
-   Github projects for ARCore: https://github.com/google-ar
-   AR experiments for inspiration and to see what could be possible: https://experiments.withgoogle.com/ar


# Enable cellphone debug
Enable developer options and debugging

On Android 4.1 and lower, the Developer options screen is available by default. On Android 4.2 and higher, you must enable this screen as follows:

-   Open the Settings app.
-   (Only on Android 8.0 or higher) Select System.
-   Scroll to the bottom and select About phone.
-   Scroll to the bottom and tap Build number 7 times.
-   Return to the previous screen to find Developer options near the bottom.

At the top of the Developer options screen, you can toggle the options on and off (figure 1). You probably want to keep this on. When off, most options are disabled except those that don't require communication between the device and your development computer.

Next, you should scroll down a little and enable USB debugging. This allows Android Studio and other SDK tools to recognize your device when connected via USB, so you can use the debugger and other tools.

The rest of this page describes some of the other options available on this screen.

# Add more icons

## 3D objects
## thumbs (Dones)




# Add wireless model factory 
## give name when add an icon and generate the model depend on the icon
## calculate the wifi signal
## show in 2d menu or bubble
