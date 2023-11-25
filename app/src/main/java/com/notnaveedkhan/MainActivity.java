package com.notnaveedkhan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.notnaveedkhan.dao.OverlayDao;
import com.notnaveedkhan.database.ApplicationDatabase;
import com.notnaveedkhan.entity.Overlay;
import com.notnaveedkhan.entity.OverlayAdapter;
import com.notnaveedkhan.exception.InvalidRequestException;
import com.notnaveedkhan.service.CustomService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private Button createButton;
    private EditText xInputField;
    private EditText yInputField;
    private EditText widthInputField;
    private EditText heightInputField;
    private EditText opacityInputField;
    private SwitchMaterial movableSwitch;
    private EditText idInputField;
    private Button deleteButton;
    private OverlayDao overlayDao;
    private ListView listView;
    private List<Overlay> overlays;
    private List<View> views;

    private void initializeViews() {
        createButton = findViewById(R.id.create_btn);
        xInputField = findViewById(R.id.x_input_field);
        yInputField = findViewById(R.id.y_input_field);
        widthInputField = findViewById(R.id.width_input_field);
        heightInputField = findViewById(R.id.height_input_field);
        opacityInputField = findViewById(R.id.opacity_input_field);
        movableSwitch = findViewById(R.id.movable_switch);
        idInputField = findViewById(R.id.id_input_field);
        deleteButton = findViewById(R.id.delete_btn);
        listView = findViewById(R.id.overlay_list);
    }

    private void init() {
        views = new ArrayList<>();
        overlays = new ArrayList<>();
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initializeViews();
        initializeDatabase();
        requestPermissions();
        loadOverlayData();
        startService(CustomService.class);
        setupListeners();
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Overlay Permission Required", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOverlayData() {
        CompletableFuture.supplyAsync(() -> {
            overlays.clear();
            overlays.addAll(overlayDao.getAll());
            return overlays;
        }).thenAcceptAsync(its -> {
            updateOverlayListView(its);
            removeOverlays();
            runOnUiThread(() -> displayOverlayData(its));
        }).exceptionally(e -> {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Error loading overlays", Toast.LENGTH_SHORT).show());
            return null;
        });
    }

    private void removeOverlays() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        for (View view : views) {
            windowManager.removeView(view);
        }
        views.clear();
    }

    private void displayOverlayData(List<Overlay> overlays) {
        for (Overlay overlay : overlays) {
            createOverlay(overlay.x, overlay.y, overlay.height, overlay.width, overlay.color, overlay.opacity, overlay.movable);
        }
    }

    private void updateOverlayListView(List<Overlay> overlays) {
        runOnUiThread(() -> listView.setAdapter(new OverlayAdapter(this, overlays)));
    }

    private void setupListeners() {
        createButton.setOnClickListener(it -> onCreateButtonClicked());
        deleteButton.setOnClickListener(it -> onDeleteButtonClicked());
    }

    private void onDeleteButtonClicked() {
        String string = idInputField.getText().toString();
        idInputField.setText("");
        if (!string.isEmpty()) {
            try {
                int id = Integer.parseInt(string);
                CompletableFuture.runAsync(() -> overlayDao.deleteById(id))
                        .thenRun(() -> runOnUiThread(this::loadOverlayData));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Overlay ID", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Enter Overlay ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCreateButtonClicked() {
        try {
            Overlay overlay = getOverlayInputData();
            CompletableFuture.supplyAsync(() -> overlayDao.existsByXAndY(overlay.x, overlay.y))
                    .thenAcceptAsync(exists -> {
                        if (!exists) {
                            overlayDao.insert(overlay);
                            runOnUiThread(this::loadOverlayData);
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, "Overlay already exists", Toast.LENGTH_SHORT).show());
                        }
                    }).exceptionally(e -> {
                        runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                        return null;
                    });
        } catch (InvalidRequestException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Overlay getOverlayInputData() {
        String stringX = xInputField.getText().toString();
        xInputField.setText("");
        if (stringX.isEmpty()) {
            throw new InvalidRequestException("X coordinate is required");
        }
        String stringY = yInputField.getText().toString();
        yInputField.setText("");
        if (stringY.isEmpty()) {
            throw new InvalidRequestException("Y coordinate is required");
        }
        String stringHeight = heightInputField.getText().toString();
        heightInputField.setText("");
        if (stringHeight.isEmpty()) {
            throw new InvalidRequestException("Height is required");
        }
        String stringWidth = widthInputField.getText().toString();
        widthInputField.setText("");
        if (stringWidth.isEmpty()) {
            throw new InvalidRequestException("Width is required");
        }
        String stringOpacity = opacityInputField.getText().toString();
        opacityInputField.setText("");
        if (stringOpacity.isEmpty()) {
            throw new InvalidRequestException("Opacity is required");
        }
        Overlay overlay = new Overlay();
        try {
            overlay.x = Integer.parseInt(stringX);
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid X coordinate");
        }
        try {
            overlay.y = Integer.parseInt(stringY);
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid Y coordinate");
        }
        try {
            overlay.height = Integer.parseInt(stringHeight);
            if (overlay.height <= 0) {
                throw new InvalidRequestException("Invalid height");
            }
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid height");
        }
        try {
            overlay.width = Integer.parseInt(stringWidth);
            if (overlay.width <= 0) {
                throw new InvalidRequestException("Invalid width");
            }
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid width");
        }
        try {
            overlay.opacity = Float.parseFloat(stringOpacity);
            if (overlay.opacity < 0 || overlay.opacity > 1) {
                throw new InvalidRequestException("Invalid opacity");
            }
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid opacity");
        }
        overlay.movable = movableSwitch.isChecked();
        movableSwitch.setChecked(false);
        overlay.color = Color.BLACK;
        return overlay;
    }

    private void startService(Class<?> clazz) {
        Intent customServiceIntent = new Intent(this, clazz);
        startService(customServiceIntent);
    }

    private void requestPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        }
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void initializeDatabase() {
        ApplicationDatabase database = Room.databaseBuilder(this, ApplicationDatabase.class, "touch_disabler").build();
        overlayDao = database.overlayDao();
    }

    private void createOverlay(int x, int y, int height, int width, int color, float opacity, boolean movable) {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        View view = new View(this);
        views.add(view);
        view.setBackgroundColor(color);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;
        params.height = height;
        params.width = width;
        params.alpha = opacity;
        windowManager.addView(view, params);
        if (movable) {
            view.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                private long latestPressTime = 0;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            if (latestPressTime == 0 || latestPressTime + 500 < System.currentTimeMillis()) {
                                latestPressTime = System.currentTimeMillis();
                            }

                            return true;
                        case MotionEvent.ACTION_UP:
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(view, params);
                            return true;
                    }
                    return false;
                }
            });
        }
    }
}