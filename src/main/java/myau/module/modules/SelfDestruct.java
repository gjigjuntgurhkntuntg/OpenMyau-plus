package myau.module.modules;

import myau.Myau;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.BooleanProperty;
import myau.property.properties.TextProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;

public class SelfDestruct extends Module {
    public static boolean destruct = false;

    public BooleanProperty replaceMod = new BooleanProperty("Replace Mod", true);
    public BooleanProperty saveLastModified = new BooleanProperty("Save Last Modified", true);
    public TextProperty downloadURL = new TextProperty("Replace URL", "https://github.com/rafaelrojasmiliani/CustomOreGen/raw/master/customoregen_1.8.9.jar");

    private static final Minecraft mc = Minecraft.getMinecraft();
    private long lastModified;
    private File modFile;

    public SelfDestruct() {
        super("Self Destruct", false, true, "Removes the client from your game");
    }

    @Override
    public void onEnabled() {
        destruct = true;
        setEnabled(false);

        if (mc.currentScreen != null) {
            mc.displayGuiScreen(null);
        }

        if (replaceMod.getValue()) {
            replaceModFile();
        }

        destroyModules();

        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i <= 10; i++) {
            runtime.gc();
            runtime.runFinalization();
            try {
                Thread.sleep(100L * i);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void replaceModFile() {
        try {
            File currentJar = getCurrentJarPath();
            if (currentJar != null && currentJar.exists() && currentJar.isFile()) {
                lastModified = currentJar.lastModified();
                downloadAndReplace(currentJar);
                if (saveLastModified.getValue()) {
                    currentJar.setLastModified(lastModified);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private File getCurrentJarPath() {
        try {
            return new File(SelfDestruct.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (Exception e) {
            return null;
        }
    }

    private void downloadAndReplace(File savePath) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(downloadURL.getValue()).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (InputStream in = connection.getInputStream();
             FileOutputStream fos = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        connection.disconnect();
    }

    private void destroyModules() {
        for (Module module : Myau.moduleManager.modules.values()) {
            try {
                module.setEnabled(false);
            } catch (Exception ignored) {
            }
        }

        Field modifiersField;
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
        } catch (Exception e) {
            return;
        }

        for (Module module : Myau.moduleManager.modules.values()) {
            try {
                Field nameField = Module.class.getDeclaredField("name");
                nameField.setAccessible(true);
                modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);
                nameField.set(module, null);

                Field descField = Module.class.getDeclaredField("description");
                descField.setAccessible(true);
                modifiersField.setInt(descField, descField.getModifiers() & ~Modifier.FINAL);
                descField.set(module, null);
            } catch (Exception ignored) {
            }
        }

        for (ArrayList<Property<?>> props : Myau.propertyManager.properties.values()) {
            for (Property<?> prop : props) {
                try {
                    Field nameField = Property.class.getDeclaredField("name");
                    nameField.setAccessible(true);
                    modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);
                    nameField.set(prop, null);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static boolean isActive() {
        return destruct;
    }
}
