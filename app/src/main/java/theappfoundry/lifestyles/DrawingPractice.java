package theappfoundry.lifestyles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.widget.LinearLayout;

public class DrawingPractice extends AppCompatActivity {

    private final String TAG = "Drawing Practice";

    private CustomDrawableView mCustomDrawableView; // Extends view and makes the rectangle.
    private LinearLayout myLinearLayout;

    public float actionBarHeight;
    public float titleBarHeight;

    private Rect outline; // Making rectangle outline
    private Paint strokePaint; // For rectangle outline color, thickness (stroke)
    private Paint paint; // setting fill color of rectangle.


    private Canvas c;
    private Bitmap b;
    private Paint p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_practice);
        Log.d(TAG, "onCreate: ");

        setBarHeights(); // see func. definition.
        setColorPaint(); // For setting color of rectangle. Don't need to recreate
        myLinearLayout = (LinearLayout)findViewById(R.id.drawingPad);
       // myLinearLayout.g
        mCustomDrawableView = new CustomDrawableView(this); // Make instance of CustomDrawableView
    }


    /**
     * Makes the CustomDrawableView ( The rectangle ) Has all the rectangles attributes as variables
     * These attributes are updated within it's draw method that is called when a MotionEvent occurs
     * Inside the draw method the rectangle and it's outline (stroke) are also created.
     * This class extends view which invokes the onDraw callback method. This is where the shape
     * is draw with the canvas that is passed in.
     */
    public class CustomDrawableView extends View {


        public ShapeDrawable mDrawable;
        public float x = 0;
        public float y = 0;
        public float width;
        public float height;
        public final int lightBlue = ContextCompat.getColor(getContext(), R.color.lightBlue);


        public CustomDrawableView(Context context) {
            super(context);
            mDrawable = new ShapeDrawable(new RectShape());
        }

        public void draw(MotionEvent event) {



            this.width = event.getX();
            this.height = event.getY() - (actionBarHeight + titleBarHeight);


//            Testing to see if values are correct.
//            Log.d(TAG, "Width: " + this.width);
//            Log.d(TAG, "height: " + this.height);
//            Log.d(TAG, "x: " + this.x);
//            Log.d(TAG, "y: " + this.y);


            mDrawable.getPaint().setColor(lightBlue); // Color of rectangle



            /**
             * Left,Top,Right,Bottom: where width = right - left, and height = bottom - top
             * Do min and max for these (x & width) and (y and height) to find absolute values.
             * You can't have negative values for setBounds or it won't draw.
             * It would be negative because of how you find the height and width.
             */
            mDrawable.setBounds(Math.min(Math.round(x),Math.round(width)),
                        Math.min(Math.round(y), Math.round(height)),
                        Math.max(Math.round(x), Math.round(width)),
                        Math.max(Math.round(y), Math.round(height)));


            // Have to apply the same rules as above when we set the bounds of mDrawable.
            outline = new Rect(Math.min(Math.round(x),Math.round(width)),
                    Math.min(Math.round(y), Math.round(height)),
                    Math.max(Math.round(x), Math.round(width)),
                    Math.max(Math.round(y), Math.round(height)));

//
//            @Override
//            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//                super.onSizeChanged(w, h, oldw, oldh);
//                mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//                mCanvas = new Canvas(mBitmap);
//            }

        }


        /**
         * Callback function for extending a view. Where stuff will be drawn onto the screen
         * @param canvas - Have to draw through this canvas, which is actually being drawn on
         *               whatever bitmap that is passed to the canvas (Done automatically
         */
        @Override
        protected void onDraw(Canvas canvas) {
//            Bitmap beta = Bitmap.createBitmap(1000, 1000,
//                    Bitmap.Config.ARGB_8888);
//
//            Canvas canvast  = new Canvas(beta);
//            mDrawable.draw(canvast);
            c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mDrawable.draw(c);
            canvas.drawBitmap(b,0,0,p);
           // canva
           // mDrawable.draw(canvas); // canvas handles what to draw.

//            canvas.drawRect(outline, strokePaint);
//            canvas.scale(100,100);
//            // Canvas acts like a pen, drawsRect onto bitmap

        }



    }

    protected void onStart() {
        super.onStart();
        setUpCanvas();


    }

    /**
     * The activity Bar + the Status bar throw off Where exactly the drawing occurs for the
     * rectangle. So have to calculate these heights and subtract them from both the y, and the
     * height from CustomShapeDrawable. I believe it's because the Motion Event is tracking the
     * whole screen but the LinearLayout that the view (CustomViewDrawable) is being placed in
     * is not the whole screen. --- Calculate Activity Bar & Status Bar heights
     */
    public void setBarHeights() {

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        titleBarHeight = contentViewTop - statusBarHeight;

        Log.d(TAG, "setBarHeights: " + titleBarHeight);

    }


    // This example shows an Activity, but you would use the same approach if
// you were subclassing a View.

    /**
     * Used for finding the x,y cordinates of the screen for both the initial touch
     * and if they drag. This can be used to update the size of the rectangle being drawn.
     * @param event - passes in a MotionEvent when scren is touched in any way
     * @return true if screen is touched
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN): // Finger touches down on the screen
                Log.d(TAG, "Action was DOWN");

                if (mCustomDrawableView.getParent() != null) {
                    mCustomDrawableView.getParent().requestDisallowInterceptTouchEvent(true);
                }
                // Rectangle may have been drawn somewhere else.
                myLinearLayout.removeAllViews(); // remove old one
                mCustomDrawableView.x = event.getX(); // Where finger is touching. update x,y
                mCustomDrawableView.y = event.getY() - (actionBarHeight + titleBarHeight);
                mCustomDrawableView.draw(event); // Might not need to call. No wid/height on// initial touch
                myLinearLayout.addView(mCustomDrawableView);

                return true;
            case (MotionEvent.ACTION_MOVE): // Finger moves while touching the screen
                Log.d(TAG, "Action was MOVE");
                myLinearLayout.removeAllViews(); // remove old drawn rectangle from LinearLayout
                mCustomDrawableView.draw(event); // draw new rectangle passing in the MotionEvent
                myLinearLayout.addView(mCustomDrawableView); //Once it's been drawn.. add to LinearLayout

                return true;

            // Finger comes up off the screen.
            // Record screen cordinates of rectangle.
            case (MotionEvent.ACTION_UP):
                Log.d(TAG, "Action was UP");

                float left = mCustomDrawableView.x;
                float top = mCustomDrawableView.y;
                float right = mCustomDrawableView.getRight();
                float bot = mCustomDrawableView.getBottom();
                Log.d(TAG, "Left: " + left + " Top: " + top + " Right: " + right + "Bottom: " + bot);
                return true;
            case (MotionEvent.ACTION_CANCEL):
                Log.d(TAG, "Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                Log.d(TAG, "Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                return super.onTouchEvent(event);
        }


    }


    /**
     * Setting the stroke of a rectangle which acts as an outline to our CustomDrawableView
     * paint handles, How to draw
     */
    public void setColorPaint(){

        paint = new Paint();
        strokePaint = paint;
        strokePaint.setARGB(255, 51, 89, 150);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(10);
    }




///////////////////////////////// Attempted Implementation ////////////////////////////////////////

/**
 * Set up bitmap and canvas.. couldn't get it to work properly. used one supplied in onDraw
 */
        public void setUpCanvas() {

            /**
             * Getting our LinearLayout which has our map in it. (For finding width/height of canvas)
             */
            //LinearLayout mapCanvas = (LinearLayout)findViewById(R.id.mapLayout);

            /**
             * Create bitmap which the drawing will be in., wid, height, Config.
             * ARGB_8888 is each pixel is stored on 8 bytes. Basically changes how its stored
             */
            //Log.d(TAG, "setUpBitmap: " + mapCanvas.getWidth());
            b = Bitmap.createBitmap(1020, 1920,
                    Bitmap.Config.ARGB_8888);



            /**
             * A Canvas works for you as a pretense, or interface, to the actual surface upon which
             * your graphics will be drawn — it holds all of your "draw" call
             */
            c = new Canvas(b); // Have to draw through the canvas using the underlying bitmap



            p = new Paint();
            p.setAlpha(150);

        }











}






