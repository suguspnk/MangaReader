package mangareader;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.control.ComboBox;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;

import com.alee.laf.WebLookAndFeel;

/**
 *
 * @author antonio
 */
public class MangaReader extends JFrame implements ActionListener{
    private static JLabel lblMangaPage, lblImgPath, lblPageNum;
    private static JComboBox<String> comboBoxImgPaths;
    private static JScrollPane sp;
	private JScrollBar vb;
	private JScrollBar hb;
	private JMenuItem mntmControls;
    private int page = 0, counter = 0;
    private byte[][] imgBytes;
    private String[] paths;
    private JPanel mangaPanel;
    private JPanel infopanel;
	private JMenu mnHelp;
	protected Point loc;
	protected int locVb;
	protected int locHb;
	private static MangaReader instance;
	private String path = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath().replace("\\", "/")+"/Manga Reader/", 
			recentImgsTmpFilename = "recentImages.tmp", currentPageTmpFilename = "currentPage.tmp";
	private Dimension windowSize = null;
	private boolean memory_efficient = true;
	
	public static MangaReader getInstance(){
		if(instance == null)
			try {
				instance = new MangaReader();
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
				Helper.printException(e);
				return null;
			}
		return instance;
	}
	
    public MangaReader() throws UnsupportedLookAndFeelException {
    	super("Manga Reader");
    	UIManager.setLookAndFeel(new WebLookAndFeel());
        addComponents();
        addListeners();
        setFrame();
//        memory_efficient = JOptionPane.showConfirmDialog(null, "Make MangaReader memory efficient? (Longer transition time between pages than normal but high image capacity)", "Choose Version", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        new Thread(new Runnable() {
			public void run() {
				loadRecentImages();
				gotoPrevCurrentPage();
			}
		}).start();
    }

	private void gotoPrevCurrentPage() {
		File file = new File(path+currentPageTmpFilename);
		String content = Helper.getTextFromFile(file);
		if(content.trim().isEmpty()){
			comboBoxImgPaths.setSelectedIndex(0);
			return;
		}
		content = content.trim();
		try{
			int page = Integer.parseInt(content);
			comboBoxImgPaths.setSelectedIndex(page);
		}catch(Exception e){
			comboBoxImgPaths.setSelectedIndex(0);
			e.printStackTrace();
			Helper.printException(e);
		}
	}

	private void loadRecentImages() {
		File recentImgsFile = new File(path+recentImgsTmpFilename);
		if(!recentImgsFile.exists())
			return;
		try {
			List<File> recentFiles = new ArrayList<File>();
			FileReader fRdr = new FileReader(recentImgsFile);
			BufferedReader rdr = new BufferedReader(fRdr);
			for(String line = ""; (line = rdr.readLine()) != null; ){
				line = line.trim();
				if(line.isEmpty())
					continue;
				File file = new File(line);
				if(!file.exists())
					continue;
				recentFiles.add(file);
			}
			rdr.close();
			fRdr.close();
			loadImgBytes(recentFiles);
		} catch (Exception e) {
			e.printStackTrace();
			Helper.printException(e);
		}
	}

	private void addListeners() {
    /*	mangaPage.addMouseMotionListener(new MouseAdapter() {
    		
    		public void mouseMoved(MouseEvent e) {
    			Point loc = e.getPoint();
    			Component comp = mangaPage.getComponentAt(loc), imgs[] = mangaPage.getComponents();
    			int i = 0;
    			for(i = 0; i < imgs.length && !imgs[i].equals(comp); i++);
    			page = i == imgs.length ? page : i;
    		}
		});*/
    	
    	mangaPanel.addMouseWheelListener(new MouseAdapter() {
    		
    		public void mouseWheelMoved(MouseWheelEvent e) {
    			int temp = vb.getValue();
				vb.setValue(vb.getValue()+(e.getPreciseWheelRotation() > 0 ? 75 : -75));
				if(temp == vb.getValue()){
					int htmp = hb.getValue();
					hb.setValue(hb.getValue()+(e.getPreciseWheelRotation() > 0 ? 75 : -75));
					if(htmp == hb.getValue()){
						if(++counter >= 7)
							comboBoxImgPaths.setSelectedIndex(page+(e.getPreciseWheelRotation() < 0 ? -1 : 1));
					}
					else
						counter = 0;
				}
				else
					counter = 0;
    		}
		});
    	
    	mangaPanel.addMouseListener(new MouseAdapter() {
            
            public void mousePressed(MouseEvent e) {
            	mangaPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                loc = e.getPoint();
                locHb = hb.getValue();
                locVb = vb.getValue();
            }
            
            
            public void mouseReleased(MouseEvent arg0) {
            	mangaPanel.setCursor(getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));
            }
        });
    	
        mangaPanel.addMouseMotionListener(new MouseMotionAdapter() {
            
            public void mouseDragged(MouseEvent e) {
                Point panelLoc = e.getPoint();
                hb.setValue(hb.getValue()-(panelLoc.x - loc.x));
                vb.setValue(vb.getValue()-(panelLoc.y - loc.y));
            }
        });
        
        addKeyListener(new KeyAdapter() {
    		@SuppressWarnings("deprecation")
			
    		public void keyPressed(KeyEvent e) {
    			if(threadAnimator!=null && threadAnimator.isAlive()){
    				if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
    					threadAnimator.stop();
    					setTitle(paths[page]);
    					lblImgPath.setText("  "+paths[page]+"  ");
    				}
    				return;
    			}
    				
    			if(e.getKeyCode() == KeyEvent.VK_DOWN)
    				vb.setValue(e.isShiftDown() ? vb.getMaximum() : vb.getValue()+(e.isControlDown() ? 250 : 50));
    			else if(e.getKeyCode() == KeyEvent.VK_UP)
    				vb.setValue(e.isShiftDown() ? 0 : vb.getValue()-(e.isControlDown() ? 250 : 50));
    			else if(e.getKeyCode() == KeyEvent.VK_LEFT)
    				hb.setValue(e.isShiftDown() ? 0 : hb.getValue()-(e.isControlDown() ? 250 : 50));
    			else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
    				hb.setValue(e.isShiftDown() ? hb.getMaximum() : hb.getValue()+(e.isControlDown() ? 250 : 50));
    			else if(e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
    				comboBoxImgPaths.setSelectedIndex(page+1);
    			else if(e.getKeyCode() == KeyEvent.VK_PAGE_UP)
    				comboBoxImgPaths.setSelectedIndex(page-1);
    			else if(e.getKeyCode() == KeyEvent.VK_HOME)
    				comboBoxImgPaths.setSelectedIndex(0);
    			else if(e.getKeyCode() == KeyEvent.VK_END && paths != null)
    				comboBoxImgPaths.setSelectedIndex(paths.length-1);
    			else if((e.getKeyCode() == KeyEvent.VK_EQUALS || e.getKeyCode() == KeyEvent.VK_MINUS) && e.isControlDown())
    				sliderZoom.setValue(sliderZoom.getValue()+(e.getKeyCode() == KeyEvent.VK_EQUALS ? 10 : -10));
    			else if(e.getKeyCode() == KeyEvent.VK_F)
    				toogleFullScreen(!isUndecorated());
    			else if(e.getKeyCode() == KeyEvent.VK_ESCAPE && isUndecorated())
    				toogleFullScreen(false);
    		}
		});
        
        addWindowListener(new WindowAdapter() {
        	
        	public void windowClosing(WindowEvent arg0) {
        		if(JOptionPane.showConfirmDialog(MangaReader.getInstance(), "Are you sure you want to exit?", "Exit Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        			System.exit(0);
        	}
		});
        
        sliderZoom.addChangeListener(new ChangeListener() {
        	Thread runningThread, nextThread;
        	
			public void stateChanged(ChangeEvent e) {
				Thread thread = new Thread(new Runnable() {
					
					public void run() {
						comboBoxImgPaths.setSelectedIndex(page);
						runningThread = nextThread;
						nextThread = null;
					}
				});
				if(runningThread == null){
					runningThread = thread;
					runningThread.start();
				}
				else{
					if(nextThread == null){
						nextThread = thread;
						thread.start();
					}
				}
			}
		});
	}

	protected void toogleFullScreen(boolean fullscreen) {
		dispose();
		if(!fullscreen)
			setSize(windowSize);
		else{
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = env.getDefaultScreenDevice();
			DisplayMode dm = gd.getDisplayMode();
			windowSize = getSize();
			setSize(dm.getWidth(), dm.getHeight());
		}
		setLocationRelativeTo(null);
		setUndecorated(!isUndecorated());
		setVisible(true);
	}

	Image img = null;
	protected void setPageImage() {
		int value = sliderZoom.getValue();
		lblZoom.setText(""+value+"%");
		img = null;
		if(memory_efficient)
			img = new ImageIcon(paths[page]).getImage();
		else
			img = new ImageIcon(imgBytes[page]).getImage();
		int w = (int) (img.getWidth(null)*(value/100f)), h = (int) (img.getHeight(null)*(value/100f));
//		Image resImg = ;
		lblMangaPage.setIcon(new ImageIcon(value == 100 ? img : Helper.resizeImage(img, w, h)));
		lblMangaPage.setPreferredSize(new Dimension(w+50, h+50));
		lblMangaPage.repaint();
		Runtime.getRuntime().gc();
	}

	boolean locked = false;
	private JLabel lblSpeed;
	private JSpinner spnrSpeed;
	
	private void changePage(int page) {
		if(locked || paths == null || page < 0 || page >= paths.length)
			return;
		locked = true;
		String str = threadAnimator!=null &&threadAnimator.isAlive() ? "(Animating)" : "";
		this.page = page;
		setPageImage();
		lblPageNum.setText("  "+(page+1)+"/"+paths.length+"  ");
		lblImgPath.setText("  "+paths[page]+"  "+str);
		setTitle(paths[page]+" "+str);
		vb.setValue(0);
		hb.setValue(0);
		sp.updateUI();
		counter = 0;
		updateCurrentPageFile(this.page);
		System.gc();
		locked = false;
	}

	private void updateCurrentPageFile(int page) {
		createMangaReaderTempDir(path);
		File file = new File(path+currentPageTmpFilename);
		try {
			FileWriter wr = new FileWriter(file);
			wr.write(""+page);
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
			Helper.printException(e);
		}
	}

	private void setFrame() {
    	setSize(800,600);
    	setMinimumSize(new Dimension(800, 600));
    	setLocationRelativeTo(null);
    	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	Helper.showOptionPaneException = true;
	}

	/**
     * @param args the command line arguments
	 * @throws UnsupportedLookAndFeelException 
     */
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        MangaReader.getInstance().setVisible(true);
    }

    private void addComponents() {
    	JMenuBar menuBar = new JMenuBar();
    	setJMenuBar(menuBar);
    	
    	mnEdit = new JMenu("Edit");
    	mnEdit.setMnemonic(KeyEvent.VK_E);
    	menuBar.add(mnEdit);
    	
    	mntmGoto = new JMenuItem("Go to Page");
    	mntmGoto.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
    	mntmGoto.addActionListener(this);
    	mnEdit.add(mntmGoto);
    	
    	mnHelp = new JMenu("Help");
    	mnHelp.setMnemonic(KeyEvent.VK_H);
    	menuBar.add(mnHelp);
    	
    	mntmControls = new JMenuItem("Controls");
    	mntmControls.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
    	mntmControls.addActionListener(this);
    	mnHelp.add(mntmControls);
    	
    	mnAnimate = new JMenu("Animate");
    	menuBar.add(mnAnimate);
    	
    	mntmAnimate = new JMenuItem("animate");
    	mntmAnimate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
    	mntmAnimate.addActionListener(this);
    	mnAnimate.add(mntmAnimate);
    	
    	mangaPanel = new JPanel();
    	mangaPanel.setLayout(new FlowLayout());
    	mangaPanel.setBackground(Color.black);
    	mangaPanel.setCursor(getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));
    	
    	lblMangaPage = new JLabel();
    	lblMangaPage.setHorizontalAlignment(SwingConstants.CENTER);
//    	mangaPage.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 50));
//    	mangaPage.setBackground(Color.black);
    	mangaPanel.add(lblMangaPage);
    	
    	sp = new JScrollPane(mangaPanel);
        sp.setWheelScrollingEnabled(true);
        getContentPane().add(sp, BorderLayout.CENTER);
        vb = sp.getVerticalScrollBar();
        hb = sp.getHorizontalScrollBar();
//        sp.getViewport().setBackground(Color.black);
//        sp.getViewport().setLayout(new FlowLayout());

        infopanel = new JPanel();
        infopanel.setLayout(new BorderLayout());
        infopanel.setPreferredSize(new Dimension(10, 25));
        getContentPane().add(infopanel, BorderLayout.SOUTH);
        
        lblImgPath = new JLabel();
        comboBoxImgPaths = new JComboBox<>(new String[]{"Images"});
        comboBoxImgPaths.setFocusable(false);
        comboBoxImgPaths.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changePage(comboBoxImgPaths.getSelectedIndex());
			}
		});
        infopanel.add(comboBoxImgPaths, BorderLayout.WEST);
        
        lblPageNum = new JLabel();
        infopanel.add(lblPageNum, BorderLayout.EAST);
        
        panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 25));
        panel.setMinimumSize(new Dimension(300, 25));
        infopanel.add(panel, BorderLayout.CENTER);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        lblZoom = new JLabel("100%");
        lblZoom.setHorizontalAlignment(SwingConstants.RIGHT);
        lblZoom.setPreferredSize(new Dimension(40, 14));
        panel.add(lblZoom);
        
        btnZoomOut = new JButton("-");
        btnZoomOut.addActionListener(this);
        btnZoomOut.setFocusable(false);
        btnZoomOut.setPreferredSize(new Dimension(40, 23));
        panel.add(btnZoomOut);
        
        sliderZoom = new JSlider();
        sliderZoom.setFocusable(false);
        sliderZoom.setMinimumSize(new Dimension(150, 25));
        sliderZoom.setMinimum(10);
        sliderZoom.setMaximum(250);
        sliderZoom.setValue(100);
        sliderZoom.setPreferredSize(new Dimension(140, 25));
        panel.add(sliderZoom);
        
        btnZoomIn = new JButton("+");
        btnZoomIn.addActionListener(this);
        btnZoomIn.setFocusable(false);
        btnZoomIn.setPreferredSize(new Dimension(40, 23));
        panel.add(btnZoomIn);
        
        new FileDrop(mangaPanel, new FileDrop.Listener() {
            public void filesDropped(final java.io.File[] files) {
            	new Thread(new Runnable() {
					
					public void run() {
						try {
							loadImgBytes(Arrays.asList(files));
							comboBoxImgPaths.setSelectedIndex(0);
							updateRecentImgsTempFile(paths);
						} catch (Exception e) {
							e.printStackTrace();
							Helper.printException(e);
						}
					}
				}).start();
            }   // end filesDropped
        });
        
        animatePanel = new JPanel();
        animatePanel.setPreferredSize(new Dimension(320, 35));
        animatePanel.setLayout(new FlowLayout());
        
        lblSpeed = new JLabel("Animation Speed (millisec): ");
        animatePanel.add(lblSpeed);
        spnrSpeed = new JSpinner(new SpinnerNumberModel(500, 1, Integer.MAX_VALUE, 100));
        animatePanel.add(spnrSpeed);
        
        System.gc();
    }

    protected void loadImgBytes(List<File> draggedFiles) throws Exception{
    	page = 0;
		ArrayList<File> list = new ArrayList<File>();
		loadFiles(list, draggedFiles);
		File files[] = new File[list.size()];
		files = list.toArray(files);
//		for(int i = 0; i < list.size(); files[i] = list.get(i), i++);
    	Arrays.sort(files);
//    	int height = 0, width = 0;
//    	mangaPage.removeAll();
    	System.gc();
    	if(!memory_efficient)
    		imgBytes = new byte[files.length][];
    	paths = new String[files.length];
    	for(int i = 0; i < files.length; i++){
    		paths[i] = files[i].getPath();
    		lblImgPath.setText("Loading "+paths[i]);
    		if(!memory_efficient){
	    		imgBytes[i] = new byte[(int) files[i].length()];
	    		try {
					new FileInputStream(files[i]).read(imgBytes[i]);
				} catch (IOException e) {
					Helper.printException(e);
					e.printStackTrace();
					imgBytes[i] = null;
				}
    		}
    	}
    	comboBoxImgPaths.setModel((new JComboBox<String>(paths)).getModel());
//    	width = width < mangaPage.getWidth() ? mangaPage.getWidth() : width;
    	list = null;
    	System.gc();
	}

	private void loadFiles(ArrayList<File> list, List<File> draggedFiles) {
		for(File file : draggedFiles){
			if(file.isDirectory()){
			/*	File childFiles[] = file.listFiles(new FilenameFilter() {
					
					public boolean accept(File file, String filename) {
						String ext = Helper.getFileType(filename);
						if(ext == null || ext.isEmpty())
							return false;
						return ext.equalsIgnoreCase("JPEG") || ext.equalsIgnoreCase("JPG") || ext.equalsIgnoreCase("PNG") || ext.equalsIgnoreCase("GIF");
					}
				});
				for(File f : childFiles)
					list.add(f);*/
				loadFiles(list, Arrays.asList(file.listFiles()));
			}
			else{
//    			String ext = Helper.getFileType(file.getName());
//				if(ext == null || ext.isEmpty())
//					continue;
//				else if (ext.equalsIgnoreCase("JPEG") || ext.equalsIgnoreCase("JPG") || ext.equalsIgnoreCase("PNG") || ext.equalsIgnoreCase("GIF"))
				list.add(file);
			}
		}
	}

	protected void updateRecentImgsTempFile(String[] paths) {
		createMangaReaderTempDir(path);
		File recentImgsFile = new File(path+recentImgsTmpFilename);
		try {
			FileWriter wr = new FileWriter(recentImgsFile);
			for(String imgPath : paths)
				wr.write(imgPath+"\n");
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
			Helper.printException(e);
		}
	}

	private void createMangaReaderTempDir(String path) {
		File dir = new File(path);
		if(!dir.exists())
			dir.mkdirs();
	}

	String controls = "<html>UP, DOWN, LEFT, RIGHT to navigate.<br>Hold CTRL+direction_key to navigate faster.<br>"
    		+ "Hold Shift+direction_key to go to the direction_key end position.<br>"
    		+ "Press F to fullscreen and ESC to exit fullscreen<br>"
    		+ "Press PAGE_UP and PAGE_DOWN to quickly change page.<br><br>"
    		+ "P.S. You can drag manga chapter folders with the pages inside the folders to the application and the application will show it accordingly.<br>"
    		+ "You can also just drag the manga images into the application to view the manga pages.</html>";
    private JMenu mnEdit;
    private JMenuItem mntmGoto;
    private JPanel panel;
    private JSlider sliderZoom;
    private JLabel lblZoom;
    private JButton btnZoomOut;
    private JButton btnZoomIn;
    private JMenu mnAnimate;
    private JMenuItem mntmAnimate;
    private JPanel animatePanel;
    
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == mntmControls)
			JOptionPane.showMessageDialog(this, controls, "Controls", JOptionPane.INFORMATION_MESSAGE);
		else if(e.getSource() == mntmGoto)
			gotoPage();
		else if(e.getSource() == mntmAnimate)
			showAnimateWindow();
		else if(e.getSource() == btnZoomIn)
			sliderZoom.setValue(sliderZoom.getValue()+10);
		else if(e.getSource() == btnZoomOut)
			sliderZoom.setValue(sliderZoom.getValue()-10);
	}

	Thread threadAnimator;
	private void showAnimateWindow() {
		if(threadAnimator != null && threadAnimator.isAlive()){
			JOptionPane.showMessageDialog(this, "Animator already running. Press ESC to stop animation.", "Animator Running", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(JOptionPane.showConfirmDialog(this, animatePanel, "Animate Properties", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
			final int speed = (int) spnrSpeed.getValue();
			threadAnimator = new Thread(new Runnable() {
				
				public void run() {
					for(int i = page; i < paths.length; Helper.delay(speed/1000f), i++)
						comboBoxImgPaths.setSelectedIndex(i);
					setTitle(paths[page]);
					lblImgPath.setText("  "+paths[page]+"  ");
				}
			});
			threadAnimator.start();
		}
	}

	private void gotoPage() {
		String input = JOptionPane.showInputDialog(this, "Page:", "Go to", JOptionPane.QUESTION_MESSAGE);
		input = input != null ? input.trim() : input;
		if(input != null && !input.isEmpty() && !(threadAnimator!=null && threadAnimator.isAlive())){
			int page;
			try{
				page = Integer.parseInt(input);
			}catch(Exception e){
				JOptionPane.showMessageDialog(this, "The input should be a page number.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			comboBoxImgPaths.setSelectedIndex(page-1);
		}
	}

}