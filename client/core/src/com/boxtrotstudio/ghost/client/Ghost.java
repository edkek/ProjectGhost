package com.boxtrotstudio.ghost.client;

import box2dLight.p3d.P3dLightManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.boxtrotstudio.ghost.client.core.game.Characters;
import com.boxtrotstudio.ghost.client.core.logic.Handler;
import com.boxtrotstudio.ghost.client.core.physics.Physics;
import com.boxtrotstudio.ghost.client.core.physics.PhysicsImpl;
import com.boxtrotstudio.ghost.client.core.render.LightCreator;
import com.boxtrotstudio.ghost.client.core.render.Text;
import com.boxtrotstudio.ghost.client.network.PlayerClient;
import com.boxtrotstudio.ghost.client.network.Stream;
import com.boxtrotstudio.ghost.client.utils.ArrayHelper;
import com.boxtrotstudio.ghost.client.utils.P2Runnable;
import com.boxtrotstudio.ghost.client.utils.PRunnable;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

public class Ghost {
    public static final AssetManager ASSETS = new AssetManager();
    public static final Physics PHYSICS = new PhysicsImpl();

    public static PlayerClient client;
    public static PlayerClient matchmakingClient;
    public static ArrayList<LightCreator> lights = new ArrayList<>();
    public static boolean isInMatch, isReady, matchStarted;

    private static GhostClient INSTANCE;
    private static Handler DEFAULT = new BlankHandler();
    public static long latency;
    public static final long UPDATE_INTERVAL = 50L;
    public static boolean isSpectating;
    public static short PLAYER_ENTITY_ID;
    public static boolean testing;

    public static Options options;

    public static String Session;
    private static Stage stage;
    private static Skin skin;

    public static Text tutorialText;
    public static Characters selfCharacter;
    public static HashMap<String, Characters> enemies = new HashMap<>();
    public static HashMap<String, Characters> allies = new HashMap<>();
    public static P3dLightManager rayHandler;
    public static boolean isDebug;
    public static long pingCount;
    @NotNull
    public static String username;
    public static int lastItem;
    public static int lastWeapon;
    public static boolean tutorial;

    public static boolean isOffline() {
        return options.hasOption("offline");
    }

    public static boolean isSSLDisabled() {
        return options != null && options.hasOption("nossl");
    }

    public static String getIp() {
        if (!options.hasOption("ip"))
            return "127.0.0.1";

        return options.getOption("ip").getValue();
    }

    public static Stream getStream() {
        for (Stream s : Stream.values()) {
            if (options.hasOption(s.name().toLowerCase()))
                return s;
        }

        return Stream.LIVE;
    }

    public static Dialog exitDialog(Skin skin) {
        Dialog dialog = new Dialog("Are you sure?", skin, "dialog") {
            protected void result(Object object) {
                boolean temp = (Boolean)object;
                if (temp)
                    Gdx.app.exit();
            }
        };

        dialog.text("Are you sure you want to logout?");
        dialog.button("Yes", true);
        dialog.button("No", false);
        dialog.key(Input.Keys.ENTER, true);
        return dialog;
    }

    public static P2Runnable<Float, Float> onMatchFound;

    public static void setDefaultHandler(Handler handler) {
        DEFAULT = handler;
    }

    public static GhostClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GhostClient(DEFAULT);
        }

        return INSTANCE;
    }

    private static boolean loaded;
    public static void loadGameAssets(AssetManager manager) {
        if (loaded)
            return;


        if (isSSLDisabled())
            disableCertificateValidation();
        else {
            try {
                addRootCA();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Load all sprites
        FileHandle[] sprites = Gdx.files.internal("sprites").list(pathname -> pathname.getName().endsWith("png") ||
                pathname.getName().endsWith("PNG") ||
                pathname.getName().endsWith("jpg") ||
                pathname.getName().endsWith("JPG"));

        //Load old menu sprites
        FileHandle[] menuSprites = Gdx.files.internal("sprites/menu").list(pathname -> pathname.getName().endsWith("png") ||
                pathname.getName().endsWith("PNG") ||
                pathname.getName().endsWith("jpg") ||
                pathname.getName().endsWith("JPG"));

        //Load start menu sprites
        FileHandle[] startMenuSprites = Gdx.files.internal("sprites/ui/start").list(pathname -> pathname.getName().endsWith("png") ||
                pathname.getName().endsWith("PNG") ||
                pathname.getName().endsWith("jpg") ||
                pathname.getName().endsWith("JPG"));

        //Load select menu sprites
        FileHandle[] selectMenuSprites = Gdx.files.internal("sprites/ui/select").list(pathname -> pathname.getName().endsWith("png") ||
                pathname.getName().endsWith("PNG") ||
                pathname.getName().endsWith("jpg") ||
                pathname.getName().endsWith("JPG"));

        FileHandle[] HUDSprites = Gdx.files.internal("sprites/ui/hud").list(pathname -> pathname.getName().endsWith("png") ||
                pathname.getName().endsWith("PNG") ||
                pathname.getName().endsWith("jpg") ||
                pathname.getName().endsWith("JPG"));

        //Load map sprites
        FileHandle[] map_files = Gdx.files.internal("maps").list(pathname -> pathname.getName().endsWith("png") ||
                pathname.getName().endsWith("PNG") ||
                pathname.getName().endsWith("jpg") ||
                pathname.getName().endsWith("JPG"));

        for (FileHandle file: ArrayHelper.combine(sprites, menuSprites, map_files, startMenuSprites, selectMenuSprites, HUDSprites)) {
            manager.load(file.path(), Texture.class);
        }

        FileHandle[] sounds = Gdx.files.internal("sounds/fx").list(pathname -> pathname.getName().endsWith("mp3") ||
               pathname.getName().endsWith("wav") ||
               pathname.getName().endsWith("ogg"));

        for (FileHandle file: sounds) {
            manager.load(file.path(), Sound.class);
        }


        //TODO Load other shit

        loaded = true;
    }

    public static void setStage(@NotNull Stage stage, @NotNull Skin skin) {
        Ghost.stage = stage;
        Ghost.skin = skin;
    }

    public static void createInfoDialog(@NotNull String title, @NotNull String text, final Runnable onOk) {
        if (stage == null || skin == null)
            return;

        Dialog dialog = new Dialog(title, skin, "dialog") {
            protected void result(Object object) {
                if (onOk != null) {
                    onOk.run();
                }
            }
        };

        dialog.text(text);
        dialog.button("Ok", true);
        dialog.key(Input.Keys.ENTER, true);

        dialog.show(stage);
    }

    public static void createQuestionDialog(@NotNull String title, @NotNull String text, final PRunnable<Boolean> onOk) {
        if (stage == null || skin == null)
            return;

        Dialog dialog = new Dialog(title, skin, "dialog") {
            protected void result(Object object) {
                if (onOk != null) {
                    onOk.run((Boolean) object);
                }
            }
        };

        dialog.text(text);
        dialog.button("Yes", true);
        dialog.button("No", false);
        dialog.key(Input.Keys.ENTER, true);

        dialog.show(stage);
    }

    private static class BlankHandler implements Handler {
        @Override
        public void start() {

        }

        @Override
        public void tick() {

        }
    }

    private static void addRootCA() throws Exception {
        if (isSSLDisabled())
            return;

        System.setProperty("https.protocols", "TLSv1"); //Fix for Java 1.8

        FileHandle x1File = Gdx.files.internal("cert/lets-encrypt-x1-cross-signed.der");
        FileHandle x2File = Gdx.files.internal("cert/lets-encrypt-x2-cross-signed.der");
        FileHandle x3File = Gdx.files.internal("cert/lets-encrypt-x3-cross-signed.der");
        FileHandle x4File = Gdx.files.internal("cert/lets-encrypt-x4-cross-signed.der");
        InputStream fis2 = x1File.read();
        InputStream fis3 = x2File.read();
        InputStream fis4 = x3File.read();
        InputStream fis5 = x4File.read();

        Certificate x1CA = CertificateFactory.getInstance("X.509").generateCertificate(fis2);
        Certificate x2CA = CertificateFactory.getInstance("X.509").generateCertificate(fis3);
        Certificate x3CA = CertificateFactory.getInstance("X.509").generateCertificate(fis4);
        Certificate x4CA = CertificateFactory.getInstance("X.509").generateCertificate(fis5);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry(Integer.toString(1), x1CA);
        ks.setCertificateEntry(Integer.toString(2), x2CA);
        ks.setCertificateEntry(Integer.toString(3), x3CA);
        ks.setCertificateEntry(Integer.toString(4), x4CA);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);

        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
    }

    public static void disableCertificateValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = (hostname, session) -> true;

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception ignored) {}
    }

    public static boolean isTesting() {
        return testing;
    }
}
