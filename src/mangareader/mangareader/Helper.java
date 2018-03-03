package mangareader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.alee.utils.ImageUtils;

public class Helper {

    /**
     * open the file in the file path
     *
     * @param path
     * @throws IOException
     * @throws InterruptedException
     */
    public static void open(String path, String parameters) throws IOException, InterruptedException {
        if (Desktop.isDesktopSupported()) {
            File myFile = new File(insertBackslash(path));
            if (!myFile.exists()) {
                JOptionPane.showMessageDialog(null, path + " does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            createTempBatFile("\"" + path + "\" " + parameters);
            String executeCmd = "temp.bat";
            Process runtimeProcess;
            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            runtimeProcess.waitFor();
        } else {
            JOptionPane.showMessageDialog(null, "Cannot open file. Not desktop supported.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        deleteTempBatFile();
    }

    private static String insertBackslash(String str) {
        String newStr = "";
        for (int i = 0; i < str.length(); newStr += str.charAt(i++)) {
            if (str.charAt(i) == '\\') {
                newStr += "\\";
            }
        }
        return newStr;
    }

    /**
     * create a temporary bat file with the name temp.bat
     *
     * @param content
     */
    public static void createTempBatFile(String content) {
        File file = new File("temp.bat");
        FileWriter in;
        BufferedWriter write;
        try {
            in = new FileWriter(file, false);
            write = new BufferedWriter(in);
            write.write(content);
            write.close();
            in.close();
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    /**
     * delete the temporary bat file
     */
    public static void deleteTempBatFile() {
        File file = new File("temp.bat");
        if (file.exists()) {
            file.delete();
        }
    }

    public static BufferedImage resizeBufferedImage(BufferedImage origImg, int IMG_WIDTH, int IMG_HEIGHT) {
        if (origImg == null) {
            return null;
        }
        try {
            int type = origImg.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : origImg.getType();
            BufferedImage resizedImg = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
            Graphics2D g = resizedImg.createGraphics();
            g = render(g);
            g.drawImage(origImg, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
            g.setComposite(AlphaComposite.Src);
            g.dispose();
            return resizedImg;
        } catch (Exception e) {
            System.err.println("resizeImage method error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return origImg;
    }
    
    public static Image resizeImage(Image origImg, int IMG_WIDTH, int IMG_HEIGHT) {
        if (origImg == null) {
            return null;
        }
        try {
            Image resizedImg = ImageUtils.resizeCanvas(new ImageIcon(origImg), IMG_WIDTH, IMG_HEIGHT).getImage();
            Graphics2D g = (Graphics2D) resizedImg.getGraphics();
            g = render(g);
            g.drawImage(origImg, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
            g.setComposite(AlphaComposite.Src);
            g.dispose();
            return resizedImg;
        } catch (Exception e) {
            System.err.println("resizeImage method error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return origImg;
    }

    public static BufferedImage rotateImage(BufferedImage origImg, double angle) {
        try {
            int type = origImg.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : origImg.getType();
            double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
            int width = origImg.getWidth(), height = origImg.getHeight();
            int newWidth = (int) Math.round(width * cos + height * sin), newHeight = (int) Math.round(height * cos + width * sin);
            BufferedImage rotatedImg = new BufferedImage(newWidth, newHeight, type);
            Graphics2D g = rotatedImg.createGraphics();
            g.translate((newWidth - width) / 2, (newHeight - height) / 2);
            g = render(g);
            g.rotate(angle, width / 2, height / 2);
            g.drawRenderedImage(origImg, null);
            g.setComposite(AlphaComposite.Src);
            g.dispose();
            return rotatedImg;
        } catch (Exception e) {
            System.err.println("rotateImage method error: " + e.getLocalizedMessage());
        }
        return origImg;
    }

    public static BufferedImage changeImgTransparency(BufferedImage origImg, float transparency) {
        try {
            int type = origImg.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : origImg.getType();
            BufferedImage resultImg = new BufferedImage(origImg.getWidth(), origImg.getHeight(), type);
            Graphics2D g = resultImg.createGraphics();
            Composite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
            g.setComposite(ac);
            g = render(g);
            g.drawImage(origImg, 0, 0, origImg.getWidth(), origImg.getHeight(), null);
            g.dispose();
            return resultImg;
        } catch (Exception e) {
            e.printStackTrace();
            //	System.err.println("transparency error!\ntransparency: "+transparency);
        }
        return origImg;
    }

    public static Graphics2D render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
        return g;
    }

    //Task: Sleep
    public static void delay(double time) {
        try {
            Thread.sleep((long) (time * 1000), (int) (((time * 1000) - (long) (time * 1000)) * 1000000));
        } catch (InterruptedException e) {
        }
    }

    /**
     * get a random number from variable from to variable to inclusive
     *
     * @param from
     * @param to
     * @return a number in [from, to] inclusive
     */
    public static int getRandomNumber(int from, int to) {
        Random random = new Random();
        int num = random.nextInt(to - from + 1);
        return from + num;
    }

    public static BufferedImage readImage(String image) {
        try {
            return ImageIO.read(new File(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage readImage(URL url) throws IOException {
        return ImageIO.read(url);
    }

    static boolean showOptionPaneException = false;

    public static void printException(Exception e) {
        System.out.println("Catched!");
        e.printStackTrace();
        if (showOptionPaneException) {
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
        }
    }

    public static void removeOpacity(JButton btn, boolean isHandCursor) {
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusable(false);
        if (isHandCursor) {
            setHandCursor(btn);
        }
    }

    public static void setHandCursor(Component comp) {
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JButton createButton(ImageIcon icon, ImageIcon ro, ActionListener listener) {
        JButton btn = new JButton(icon);
        btn.setRolloverIcon(ro);
        removeOpacity(btn, true);
        setHandCursor(btn);
        return btn;
    }

    /**
     * if the number n when multiplied with the numberType is greater than zero
     * than it is a valid number
     *
     * @param n is the number
     * @param numberType is the validity basis, if this is 1 then n should be
     * positive to be valid else if -1 then n should be negative
     * @return
     */
    public static boolean validNum(String n, int numberType) {
        int num = 0;
        try {
            num = Integer.parseInt(n);
        } catch (Exception e) {
            return false;
        }
        return num * numberType > 0;
    }

    public static byte[] buffImgToByteArray(BufferedImage image, File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, file.getName().toLowerCase().endsWith(".png") ? "png" : file.getName().toLowerCase().endsWith(".jpg") ? "jpg" : "jpeg", baos);
        } catch (IOException e) {
            Helper.printException(e);
        }
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public static BufferedImage byteArrayToBuffImage(byte[] imageInByte) {
        InputStream in = new ByteArrayInputStream(imageInByte);
        BufferedImage img = null;
        try {
            img = ImageIO.read(in);
        } catch (IOException e) {
            Helper.printException(e);
        }
        return img;
    }

    public static void println(Object obj) {
        System.out.println(obj);
    }

    public static long getTextOccurrence(String txt, String regex) {
        return txt.length() - txt.replace(regex, "").length();
    }

    public static long getIndexOfStringCount(String regex, long numOccurrence, String txt) {
        long index = -1;
        while (numOccurrence-- > 0) {
            index = txt.indexOf(txt.substring((int) (index + 1)));
        }
        return index == -1 ? 0 : index;
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String getMissing0s(String bin, int length) {
        String miss0s = "";
        int missing = length - bin.length();
        for (int i = 0; i < missing; miss0s += "0", i++);
        return miss0s;
    }

    public static String[] getTextFromFiles(File[] files) {
        String content[] = new String[files.length], line;
        for (int i = 0; i < files.length; i++) {
            try {
                FileReader reader = new FileReader(files[i]);
                BufferedReader out = new BufferedReader(reader);
                for (content[i] = ""; (line = out.readLine()) != null; content[i] += line + System.lineSeparator());
                out.close();
                reader.close();
            } catch (Exception e) {
                printException(e);
            }
        }
        return content;
    }

    /**
     * return the content of a file.
     * @param file
     * @return an empty string if the file has no content otherwise the content of the file is returned
     */
    public static String getTextFromFile(File file) {
        String content = "", line;
        try {
            FileReader reader = new FileReader(file);
            BufferedReader out = new BufferedReader(reader);
            for (content = ""; (line = out.readLine()) != null; content += line + System.lineSeparator());
            out.close();
            reader.close();
        } catch (Exception e) {
            printException(e);
        }
        return content;
    }

    public static String getHexString(int num) {
        return "0x" + Integer.toHexString(num).toUpperCase();
    }

    public static void adjustLblSize(JLabel lbl) {
        FontMetrics metrics = lbl.getFontMetrics(lbl.getFont());
        int height = metrics.getHeight();
        int width = metrics.stringWidth(lbl.getText());
        lbl.setSize(width, height);
    }

    public static void showMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Message", JOptionPane.PLAIN_MESSAGE);
    }

    public static void showInfoMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static double randomNumber(long i) {
        double rand = Math.random(), num = rand * i;
        return num;
    }

    public static void registerFont(URL url) throws Exception {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Font.createFont(Font.TRUETYPE_FONT, url.openStream()));
    }

    public static long factorial(long i) {
        return i <= 1 ? 1 : i * factorial(i - 1);
    }

    public static String getFileType(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return null;
        } else if (index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1);
    }

    public static void saveImage(BufferedImage img, String type, File file) throws IOException {
        ImageIO.write(img, type, file);
    }

    public synchronized static void setImageRGB(BufferedImage image, int i, int j, int red, int green, int blue) {
        image.setRGB(j, i, new Color(red, green, blue).getRGB());
    }

    static ArrayList<Thread> sleepingThreads = new ArrayList<Thread>();

    public static void pause() {
        sleepingThreads.add(Thread.currentThread());
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
//			e.printStackTrace();
        }
    }

    public static void wakeUpSleepingThreads() {
        for (; !sleepingThreads.isEmpty();) {
            sleepingThreads.remove(0).interrupt();
        }
    }

    public static void pauseIfPaused(boolean isPaused) {
        if (isPaused) {
            pause();
        }
    }

    public static CMDResult executeCommand(String[] command) {
        Process p;
//		String		fullCommand = kGnuPGCommand + " " + commandArgs;
//		String		fullCommand = commandArgs;

//		System.out.println (fullCommand);
//		System.out.println(Arrays.toString(command));
        try {
            p = Runtime.getRuntime().exec(command);
        } catch (IOException io) {
            System.out.println("io Error" + io.getMessage());
            return null;
        }

        ProcessStreamReader psr_stdout = new ProcessStreamReader(p.getInputStream(), "psr_stdout");
        ProcessStreamReader psr_stderr = new ProcessStreamReader(p.getErrorStream(), "psr_stderr");
        psr_stdout.start();
        psr_stderr.start();
        /*	if (inputStr != null)
         {
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
         try
         {
         out.write(inputStr);
         out.close();
         }
         catch(IOException io)
         {
         System.out.println("Exception at write! " + io.getMessage ());
         return false;
         }
         }*/

        try {
            p.waitFor();

            psr_stdout.join();
            psr_stderr.join();
        } catch (InterruptedException i) {
            System.out.println("Exception at waitfor! " + i.getMessage());
            return null;
        }

        return new CMDResult(p.exitValue(), psr_stdout.getString(), psr_stderr.getString());
    }

    static class CMDResult {

        int exitValue;
        String output_str, output_err;

        public CMDResult(int xtval, String strO, String strE) {
            exitValue = xtval;
            output_str = strO;
            output_err = strE;
        }
    }

    /**
     * Reads an output stream from an external process. Imeplemented as a thred.
     */
    static class ProcessStreamReader extends Thread {

        StringBuffer stream;
        InputStreamReader in;

        final static int BUFFER_SIZE = 1024;

        /**
         * Creates new ProcessStreamReader object.
         *
         * @param	in
         * @param name
         */
        ProcessStreamReader(InputStream in, String name) {
            super(name);

            this.in = new InputStreamReader(in);

            this.stream = new StringBuffer();
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(in);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    stream.append(line + "\n");
                }
            } catch (IOException io) {
            }
        }

        String getString() {
            return stream.toString();
        }
    }

    static Point loc;

    public static void setDraggable(final Component obj) {
        obj.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                loc = e.getPoint();
            }
        });
        obj.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point screenLoc = e.getLocationOnScreen();
                obj.setLocation(screenLoc.x - loc.x, screenLoc.y - loc.y);
            }
        });
    }

}
