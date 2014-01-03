package hr.fer.zemris.jcms.occvis;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class MainApplet extends JApplet {

	private static final long serialVersionUID = 1L;
	private String ferkoURL;
	private String datumOD;
	private String datumDO;
	private String courseInstanceID;
	private String jmbagsList;
	private String createOccupancyMap;
	private Map<String,List<Entry>> entries;
	private JLabel status;
	
	private static class Area {
		int x;
		int y;
		int w;
		int h;
		Entry e;
		
		public Area(int x, int y, int w, int h, Entry e) {
			super();
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.e = e;
		}
	}

	private Entry last_e;
	
	protected void selectedEvent(Entry e) {
		if(last_e==e) return;
		last_e = e;
		if(e==null) {
			status.setText("    ");
		} else {
			status.setText("Broj zauzetih studenata: "+e.n+". Možete kliknuti na pojedini termin za detaljni popis.");
		}
	}

	private class MyComponent extends JComponent {
		
		private static final long serialVersionUID = 1L;
		private Map<String,List<Entry>> remeberedEn = null;
		private List<Area> areas = new ArrayList<Area>();

		static final int HOURS_H_MARGIN = 5;

		public MyComponent() {
			this.addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					int x = e.getX(); 
					int y = e.getY();
					for(Area a : areas) {
						if(inbox(a, x, y)) {
							selectedEvent(a.e);
							return;
						}
					}
					selectedEvent(null);
				}
				private boolean inbox(Area a, int x, int y) {
					if(x<a.x || y<a.y) return false;
					if(x>a.x+a.w || y>a.y+a.h) return false;
					return true;
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					mouseMoved(e);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					selectedEvent(null);
				}
			});
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					Entry entry = last_e;
					if(entry==null) return;
					int i = entry.students.indexOf('|');
					String s = entry.students.substring(i+1);
					String[] elems = s.split("\\|");
					List<String> jmbags = new ArrayList<String>(elems.length);
					for(String j : elems) {
						j = j.trim();
						if(j.isEmpty()) continue;
						jmbags.add(j);
					}
					Collections.sort(jmbags);
					StringBuilder sb = new StringBuilder(jmbags.size()*15);
					for(String j : jmbags) {
						sb.append(j).append("\n");
					}
					JPanel p = new JPanel(new BorderLayout());
					p.setPreferredSize(new Dimension(400, 500));
					p.add(new JLabel(entry.n+" zauzetih studenata"), BorderLayout.PAGE_START);
					JTextArea jta = new JTextArea(sb.toString());
					jta.setEditable(false);
					JScrollPane jsp = new JScrollPane(jta);
					jsp.setPreferredSize(new Dimension(400, 450));
					p.add(jsp, BorderLayout.CENTER);
					JOptionPane.showMessageDialog(MyComponent.this, p, "Izvještaj s zauzetim studentima", JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Dimension sz = this.getSize();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, sz.width, sz.height);
			
			Map<String,List<Entry>> en = null;
			synchronized(MainApplet.this) {
				en = entries;
			}
			if(en==null) return;
			
			int[] hws = new int[24];
			String[] hs = new String[24];
			for(int i = 0; i < 10; i++) {
				hs[i] = "0"+i+":00";
			}
			for(int i = 10; i < 24; i++) {
				hs[i] = i+":00";
			}
			FontMetrics fm = g.getFontMetrics();
			int hmax = 0;
			for(int i = 0; i < hs.length; i++) {
				hws[i] = fm.stringWidth(hs[i]);
				if(hmax<hws[i]) hmax=hws[i];
			}
			
			int days = fm.getHeight();
			int hrsw = hmax+2*HOURS_H_MARGIN;
			
			double rh = (sz.height-days) / 24.0;
			double dw = (sz.width-hrsw) / ((double)en.size());
			
			boolean shouldAdd = false;
			if(en!=remeberedEn) {
				shouldAdd = true;
				remeberedEn = en;
				areas.clear();
			}
			Color grid = Color.BLACK;
			Color titles = Color.BLACK;
			g.setColor(titles);
			for(int i = 0; i < 24; i++) {
				int y = days + (int)(i*rh+0.5);
				g.drawString(hs[i], HOURS_H_MARGIN, y+fm.getAscent());
			}
			List<String> allDays = new ArrayList<String>(en.keySet());
			for(int i = 0; i < en.size(); i++) {
				int x = hrsw + (int)(i*dw+0.5);
				String day = allDays.get(i);
				g.drawString(day, x+(int)((dw-fm.stringWidth(day))/2+0.5), fm.getAscent());
			}
			
			for(String day : en.keySet()) {
				int di = allDays.indexOf(day);
				int x = hrsw + (int)(di*dw+0.5);
				int x_e = x+(int)(dw+0.5);
				List<Entry> list = en.get(day);
				for(Entry e : list) {
					int y_s = days + (int)(e.fromAbsTime/60.0*rh+0.5);
					int y_e = days + (int)(e.toAbsTime/60.0*rh+0.5);
					int col = e.n * 5;
					col = 230 - col;
					if(col<0) col = 0;
					Color c = new Color(255, col, col);
					g.setColor(c);
					g.fillRect(x, y_s, x_e-x, y_e-y_s);
					if(shouldAdd) {
						areas.add(new Area(x, y_s, x_e-x, y_e-y_s,e));
					}
					g.setColor(Color.BLACK);
					g.drawString(e.n+" zauzetih", x+4, y_s+g.getFontMetrics().getHeight());
				}
			}
			
			g.setColor(grid);
			g.drawLine(0, 0, sz.width, 0);	
			g.drawLine(sz.width-1, 0, sz.width-1, sz.height);	
			g.drawLine(sz.width-1, sz.height-1, 0, sz.height-1);
			g.drawLine(0, sz.height-1, 0, 0);
			
			for(int i = 0; i < en.size(); i++) {
				int x = hrsw + (int)(i*dw+0.5);
				g.drawLine(x, 0, x, sz.height);
			}
			for(int i = 0; i < 24; i++) {
				int y = days + (int)(i*rh+0.5);
				g.drawLine(0, y, sz.width, y);
			}
		}
		
	}
	
	@Override
	public void init() {
		super.init();
		if(SwingUtilities.isEventDispatchThread()) {
			safeInit();
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						safeInit();
					}
				});
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void start() {
		super.start();
		if(SwingUtilities.isEventDispatchThread()) {
			safeStart();
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						safeStart();
					}
				});
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private MyComponent comp;
	
	protected void safeInit() {
		this.getContentPane().setLayout(new BorderLayout());
		comp = new MyComponent();
		status = new JLabel("    ");
		this.getContentPane().add(status, BorderLayout.NORTH);
		this.getContentPane().add(comp, BorderLayout.CENTER);
		ferkoURL = getParameter("ferkourl");
		datumOD = getParameter("dateFrom");
		datumDO = getParameter("dateTo");
		courseInstanceID = getParameter("courseInstanceID");
		jmbagsList = getParameter("jmbagsList");
		createOccupancyMap = getParameter("createOccupancyMap");
		
		// i sada override:
		// ferkoURL = "http://localhost:8080/ferko/StudentScheduleAnalyzer!viewForSemesterAndUsers.action";
		// datumOD = "2009-09-07";
		// datumDO = "2009-09-11";
		// courseInstanceID = "2009Z/34559";
		// jmbagsList = "0036433138 0036431058 0036430664 0036430638 0036427833 0036427144 0036423904 0036433895";
		createOccupancyMap = "true";
	}
	
	protected void safeStart() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				fetchInThread();
			}
		}).start();
	}

	protected void fetchInThread() {
		List<String> retci = null;
		HttpURLConnection conn = null;
		try {
			try {
				URL url = new URL(ferkoURL);
				conn = (HttpURLConnection)url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
				StringBuilder sb = new StringBuilder();
				sb.append("dateFrom=").append(URLEncoder.encode(datumOD,"UTF-8")).append("&");
				sb.append("dateTo=").append(URLEncoder.encode(datumDO,"UTF-8")).append("&");
				sb.append("courseInstanceID=").append(URLEncoder.encode(courseInstanceID,"UTF-8")).append("&");
				String jmbagsToSend = jmbagsList.replace(' ', '\n');
				sb.append("jmbagsList=").append(URLEncoder.encode(jmbagsToSend,"UTF-8")).append("&");
				sb.append("createOccupancyMap=").append(URLEncoder.encode(createOccupancyMap,"UTF-8"));
				byte[] podatci = sb.toString().getBytes("UTF-8");
				conn.setRequestProperty("Content-Length", Integer.toString(podatci.length));
				OutputStream os = conn.getOutputStream();
				os.write(podatci);
				os.close();
			} catch(Exception ex) {
				ex.printStackTrace();
				return;
			}
			try {
				int responseCode = conn.getResponseCode();
				if(responseCode!=200) {
					System.err.println("Greška u komunikaciji. Vraćen status: "+responseCode);
					return;
				}
				String contentType = conn.getContentType();
				if(!contentType.equals("application/zip")) {
					System.err.println("Nisam dobio ZIP. Vraćeno je: "+contentType);
					return;
				}
				ZipInputStream zf = new ZipInputStream(new BufferedInputStream(conn.getInputStream()));
				while(true) {
					ZipEntry ze = zf.getNextEntry();
					if(ze==null) break;
					System.out.println("Ime je: "+ze.getName());
					if(!ze.getName().equals("mapaZauzetosti.csv")) {
						zf.closeEntry();
						continue;
					}
					retci = new ArrayList<String>();
					BufferedReader br = new BufferedReader(new InputStreamReader(zf,"UTF-8"));
					while(true) {
						String line = br.readLine();
						if(line==null) break;
						retci.add(line);
						System.out.println("Citam: "+line);
					}
					zf.closeEntry();
					break;
				}
				zf.close();
			} catch(Exception ex) {
				ex.printStackTrace();
				return;
			}
		} finally {
			if(conn!=null) try { conn.disconnect(); } catch(Exception ignorable) {}
		}
		if(retci==null) {
			System.err.println("Nastupila je nekakva pogreška (1).");
			return;
		}
		Map<String,List<Entry>> entries = new LinkedHashMap<String,List<Entry>>();
		if(datumOD.equals(datumDO)) {
			entries.put(datumOD, new ArrayList<Entry>());
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			try { cal.setTime(sdf.parse(datumOD)); } catch(Exception ignorable) {} // Malo prije sam mogao, zasto bi sada puklo?!
			cal.set(Calendar.HOUR, 8);
			for(int i=0; i<7; i++) {
				String sada = sdf.format(cal.getTime());
				entries.put(sada, new ArrayList<Entry>());
				if(sada.equals(datumDO)) break;
				cal.add(Calendar.HOUR_OF_DAY, 24);
			}
		}
		for(String r : retci) {
			String[] tokens = r.split(";");
			Entry e = new Entry();
			e.date = tokens[0];
			e.fromStr = tokens[1];
			e.toStr = tokens[2];
			int p = tokens[3].indexOf('|');
			e.n = Integer.parseInt(tokens[3].substring(0, p));
			e.students = tokens[3];
			e.fromAbsTime = Integer.parseInt(e.fromStr.substring(0,2))*60+Integer.parseInt(e.fromStr.substring(3,5));
			e.toAbsTime = Integer.parseInt(e.toStr.substring(0,2))*60+Integer.parseInt(e.toStr.substring(3,5));
			List<Entry> list = entries.get(e.date);
			list.add(e);
		}
		MainApplet.this.entries = entries;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainApplet.this.comp.repaint();
			}
		});
	}
	
	class Entry {
		String date;
		int fromAbsTime;
		int toAbsTime;
		int n;
		String students;
		String fromStr;
		String toStr;
	}
}
