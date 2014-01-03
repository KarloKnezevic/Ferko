package hr.fer.zemris.sscoretree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Lista svih dostupnih provjera dostavlja se u obliku tekstualnog formata čiji je svaki redak formata:<br>
 * <pre>kind(F:A) \t id \t shortName \t name.</pre><br>
 * Hijerarhija je definirana formatom koji se sastoji od nekoliko naredbi:
 * <table>
 * <tr>
 *   <td>%push</td>
 *   <td>postavlja zadnje dodanu stavku kao roditelja; sve što se u nastavku dodaje, dodavat će se kao djeca tog roditelja.</td>
 * </tr>
 * <tr>
 *   <td>%pop</td>
 *   <td>skida aktualnog roditelja i reaktivira njegovog roditelja.</td>
 * </tr>
 * <tr>
 *   <td>%add:kind(F:A):Id</td>
 *   <td>dodaje zastavicu ili provjeru sa zadanim Id-om kao dijete trenutno postavljenog roditelja.</td>
 * </tr>
 * </table>
 * 
 * @author marcupic
 *
 */
public class ScoreComponent {

	private TreeNode tree;
	TreeComponent component;
	JPanel panel;
	List<RenderedTreeNode> renderedTreeNodes = new ArrayList<RenderedTreeNode>();
	JList flat;
	List<Element> elements;
	List<ElementWrapper> elementWrappers;
	ElementWrapperListModel listModel;
	TreeNode selectedTreeNode;
	boolean changed = false;
	JButton btnDodajDijete;
	JButton btnDodajIznad;
	JButton btnDodajIspod;
	
	/**
	 * <code>flatList</code>: kind(F:A) \t id \t shortName \t name.<br>
	 * @param flatList
	 * @param hier
	 */
	public ScoreComponent(String flatList, String hier) {
		elements = readElements(flatList);
		elementWrappers = new ArrayList<ElementWrapper>();
		for(Element e : elements) {
			elementWrappers.add(new ElementWrapper(e));
		}
		listModel = new ElementWrapperListModel();
		tree = new TreeNode();
		readHierarchy(hier);
		component = new TreeComponent();
		panel = new JPanel();
		flat = new JList(listModel);
		JScrollPane listScrollPane = new JScrollPane(flat);
		listScrollPane.setPreferredSize(new Dimension(200, 100));
		panel.setLayout(new BorderLayout());
		panel.add(component, BorderLayout.CENTER);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		rightPanel.add(listScrollPane, BorderLayout.CENTER);
		rightPanel.add(buttonPanel, BorderLayout.SOUTH);
		panel.add(rightPanel, BorderLayout.EAST);
		btnDodajDijete = new JButton("Dodaj kao dijete");
		btnDodajIznad = new JButton("Dodaj iznad");
		btnDodajIspod = new JButton("Dodaj ispod");
		buttonPanel.add(btnDodajDijete);
		buttonPanel.add(btnDodajIznad);
		buttonPanel.add(btnDodajIspod);
		setSelectedTreeNode(tree);
		
		btnDodajDijete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dodajDijete();
			}
		});
		
		btnDodajIznad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dodajIznad();
			}
		});
		
		btnDodajIspod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dodajIspod();
			}
		});
		
		flat.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;
				checkAvailableCommands();
			}
		});
		
//		JButton dump = new JButton("dump");
//		buttonPanel.add(dump);
//		dump.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				String serial = serializeHierarchy();
//				System.out.println("Program je:");
//				System.out.println(serial);
//			}
//		});
	}

	public void notifySaved() {
		if(!changed) return;
		changed = false;
		component.repaint();
	}
	
	protected String serializeHierarchy() {
		StringBuilder sb = new StringBuilder(2048);
		for(TreeNode child: tree.children) {
			recursiveSerializeHierarchy(sb, child);
		}
		return sb.toString();
	}

	private void recursiveSerializeHierarchy(StringBuilder sb, TreeNode node) {
		boolean hasChildren = node.children!=null && !node.children.isEmpty();
		if(hasChildren) {
			sb.append("%addp:");
		} else {
			sb.append("%add:");
		}
		sb.append(node.element.kind).append(":").append(node.element.id).append("\r\n");
		if(node.children!=null && !node.children.isEmpty()) {
			for(TreeNode child : node.children) {
				recursiveSerializeHierarchy(sb, child);
			}
			sb.append("%pop\r\n");
		}
	}

	protected void dodajDijete() {
		if(selectedTreeNode==null) return;
		int selIndex = flat.getSelectedIndex();
		if(selIndex==-1) return;
		Object[] values = flat.getSelectedValues();
		for(Object selObject : values) {
			changed = true;
			ElementWrapper elw = (ElementWrapper)selObject;
			elw.used = true;
			listModel.elementStatusChanged(elw);
			TreeNode node = new TreeNode();
			node.element = elw.element;
			selectedTreeNode.addChild(node);
		}
		flat.clearSelection();
		component.repaint();
	}

	protected void dodajIznad() {
		if(selectedTreeNode==null) return;
		int selIndex = flat.getSelectedIndex();
		if(selIndex==-1) return;
		Object[] values = flat.getSelectedValues();
		for(Object selObject : values) {
			changed = true;
			ElementWrapper elw = (ElementWrapper)selObject;
			elw.used = true;
			listModel.elementStatusChanged(elw);
			TreeNode node = new TreeNode();
			node.element = elw.element;
			selectedTreeNode.addAbove(node);
		}
		flat.clearSelection();
		component.repaint();
	}
	
	protected void dodajIspod() {
		if(selectedTreeNode==null) return;
		int selIndex = flat.getSelectedIndex();
		if(selIndex==-1) return;
		Object[] values = flat.getSelectedValues();
		if(values.length<=0) return;
		Object[] values2 = new Object[values.length];
		for(int i = 0; i < values.length; i++) {
			values2[values.length-1-i] = values[i];
		}
		values = values2;
		for(Object selObject : values) {
			changed = true;
			ElementWrapper elw = (ElementWrapper)selObject;
			elw.used = true;
			listModel.elementStatusChanged(elw);
			TreeNode node = new TreeNode();
			node.element = elw.element;
			selectedTreeNode.addBelow(node);
		}
		flat.clearSelection();
		component.repaint();
	}
	
	private void checkAvailableCommands() {
		int selIndex = flat.getSelectedIndex();
		if(selectedTreeNode==null || selIndex==-1) {
			btnDodajDijete.setEnabled(false);
			btnDodajIznad.setEnabled(false);
			btnDodajIspod.setEnabled(false);
		} else {
			btnDodajDijete.setEnabled(true);
			btnDodajIznad.setEnabled(selectedTreeNode.addAboveSupported());
			btnDodajIspod.setEnabled(selectedTreeNode.addBelowSupported());
		}
	}
	
	private void setSelectedTreeNode(TreeNode node) {
		selectedTreeNode = node;
		checkAvailableCommands();
	}
	
	public JComponent getComponent() {
		return panel;
	}
	
	
	private void readHierarchy(String hier) {
		Stack<TreeNode> stack = new Stack<TreeNode>();
		tree.children.clear();
		stack.push(tree);
		BufferedReader br = new BufferedReader(new StringReader(hier));
		try {
			while(true) {
				String line = br.readLine();
				if(line==null) break;
				line = line.trim();
				if(line.isEmpty()) continue;
				if(line.startsWith("%addp:")||line.startsWith("%add:")) {
					String[] x = line.split(":");
					String kind = x[1];
					Long id = Long.valueOf(x[2]);
					// Pronađi takav element
					Element el = null;
					ElementWrapper elw = null;
					for(ElementWrapper e : elementWrappers) {
						if(e.element.kind.equals(kind) && e.element.id.equals(id)) {
							el = e.element;
							elw = e;
							break;
						}
					}
					if(el==null) {
						el = new Element();
						el.invalid = true;
						el.id = id;
						el.kind = kind;
						el.shortName = "INVALID";
						el.name = "Assessment or flag does not exists any more.";
					} else {
						elw.used = true;
					}
					TreeNode newNode = new TreeNode();
					newNode.element = el;
					stack.peek().addChild(newNode);
					if(x[0].equals("%addp")) {
						stack.push(newNode);
					}
				} else if(line.equals("%pop")) {
					stack.pop();
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private List<Element> readElements(String flatList) {
		if(flatList==null) return new ArrayList<Element>();
		List<Element> list = new ArrayList<Element>();
		BufferedReader br = new BufferedReader(new StringReader(flatList));
		try {
			while(true) {
				String line = br.readLine();
				if(line==null) break;
				line = line.trim();
				if(line.isEmpty()) continue;
				String[] x = line.split("\t");
				Element e = new Element();
				e.id = Long.valueOf(x[1]);
				e.kind = x[0];
				e.name = x[3];
				e.shortName = x[2];
				list.add(e);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	private class TreeComponent extends JComponent {

		private static final long serialVersionUID = 1L;
		
		int offsetX;
		int offsetY;
		int marginX = 10;
		int marginY = 10;
		int levelIndentation = 40;
		int rowHeight;

		MouseAdapter adapter = new MouseAdapter() {
			int pressX;
			int pressY;
			int originalOffsetX;
			int originalOffsetY;
			int button=-1;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(button==-1) {
					button = e.getButton();
				} else {
					return;
				}
				pressX = e.getX();
				pressY = e.getY();
				originalOffsetX = offsetX;
				originalOffsetY = offsetY;
			}
			
			public void mouseReleased(MouseEvent e) {
				if(button!=e.getButton()) {
					return;
				}
				int b = button;
				button = -1;
				if(b==MouseEvent.BUTTON1) {
					if(pressX==e.getX() && pressY==e.getY()) {
						boolean nasao = false;
						for(RenderedTreeNode n : renderedTreeNodes) {
							if(n.containsPoint(e.getX(), e.getY())) {
								nasao = true;
								if(selectedTreeNode != n.node) {
									selectedTreeNode = n.node;
									TreeComponent.this.repaint();
									checkAvailableCommands();
								}
								break;
							}
						}
						if(selectedTreeNode!=null && !nasao) {
							selectedTreeNode = null;
							TreeComponent.this.repaint();
							checkAvailableCommands();
						}
					}
				}
				TreeComponent.this.requestFocusInWindow();
			};
			
			public void mouseDragged(MouseEvent e) {
				if(button==MouseEvent.BUTTON1) {
					int deltaX = e.getX() - pressX;
					int deltaY = e.getY() - pressY;
					offsetX = originalOffsetX + deltaX;
					offsetY = originalOffsetY + deltaY;
					TreeComponent.this.repaint();
				}
			};
		};

		KeyListener keyListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("Tipka: "+e.getKeyCode());
				if(e.getKeyCode()==KeyEvent.VK_UP) {
					e.consume();
					if(selectedTreeNode!=null && selectedTreeNode.parent!=null) {
						if(selectedTreeNode.moveUp()) {
							changed = true;
							TreeComponent.this.repaint();
						}
					}
					return;
				}
				if(e.getKeyCode()==KeyEvent.VK_DOWN) {
					e.consume();
					if(selectedTreeNode!=null && selectedTreeNode.parent!=null) {
						if(selectedTreeNode.moveDown()) {
							changed = true;
							TreeComponent.this.repaint();
						}
					}
					return;
				}
				if(e.getKeyCode()==KeyEvent.VK_LEFT) {
					e.consume();
					if(selectedTreeNode!=null && selectedTreeNode.parent!=null && selectedTreeNode.parent.parent!=null) {
						if(selectedTreeNode.deIndent()) {
							changed = true;
							TreeComponent.this.repaint();
						}
					}
					return;
				}
				if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
					e.consume();
					if(selectedTreeNode!=null && selectedTreeNode.parent!=null) {
						if(selectedTreeNode.indent()) {
							changed = true;
							TreeComponent.this.repaint();
						}
					}
					return;
				}
				if(e.getKeyCode()==KeyEvent.VK_DELETE) {
					e.consume();
					if(selectedTreeNode!=null && selectedTreeNode.parent!=null) {
						if(selectedTreeNode.delete()) {
							changed = true;
							selectedTreeNode = null;
							checkAvailableCommands();
							TreeComponent.this.repaint();
							for(ElementWrapper ew : elementWrappers) {
								ew.used = false;
							}
							osvjeziKoristenje(tree);
							listModel.fireUpdatedAll();
							
						}
					}
					return;
				}
			}
			
			protected void osvjeziKoristenje(TreeNode tree) {
				if(tree.element!=null) {
					for(ElementWrapper ew : elementWrappers) {
						if(ew.element==tree.element) {
							ew.used = true;
							break;
						}
					}
				}
				for(TreeNode child : tree.children) {
					osvjeziKoristenje(child);
				}
			}
		};
		
		public TreeComponent() {
			this.setFocusable(true);
			this.addMouseListener(adapter);
			this.addMouseMotionListener(adapter);
			this.addKeyListener(keyListener);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			Dimension size = this.getSize();
			if(changed) {
				Color col = new Color(200,255,200);
				g2d.setColor(col);
			} else {
				g2d.setColor(Color.WHITE);
			}
			g2d.fillRect(0, 0, size.width, size.height);
			g2d.setColor(Color.BLACK);
			rowHeight = (int)(1.2*g2d.getFontMetrics().getHeight());
			renderedTreeNodes.clear();
			recursivePaint(g2d, size, tree, 0, 0);
		}

		/**
		 * @param g2d
		 * @param size
		 * @param tree
		 * @param level
		 * @param startRow
		 * @return koliko je redaka ovime dodano
		 */
		private int recursivePaint(Graphics2D g2d, Dimension size, TreeNode tree, int level, int startRow) {
			if(tree==null) return 0;
			int dodano = 1;
			String label = tree.element==null ? "Korijen" : tree.element.toString();
			boolean invalid = tree.element!=null && tree.element.invalid;
			int w = g2d.getFontMetrics().stringWidth(label);
			int x0 = offsetX + marginX + level*levelIndentation;
			int y0 = offsetY + marginY + startRow*rowHeight;
			int x1 = x0 + w + 6;
			int y1 = y0 + g2d.getFontMetrics().getHeight();
			renderedTreeNodes.add(new RenderedTreeNode(tree, x0, y0, x1, y1));
			if(invalid) {
				g2d.setColor(Color.RED);
			}
			g2d.drawString(label, x0 + 3, y0 + g2d.getFontMetrics().getAscent());
			if(invalid) {
				g2d.drawLine(x0, y0 + g2d.getFontMetrics().getAscent()/2, x1, y0 + g2d.getFontMetrics().getAscent()/2);
				g2d.setColor(Color.BLACK);
			}
			if(selectedTreeNode==tree) {
				g2d.setColor(Color.BLUE);
				g2d.drawRect(x0, y0, x1-x0+1, y1-y0+1);
				g2d.setColor(Color.BLACK);
			}
			for(int l = 0; l < level-1; l++) {
				g2d.drawLine(offsetX + marginX + l*levelIndentation+levelIndentation/2, y0, offsetX + marginX + l*levelIndentation+levelIndentation/2, y0+rowHeight);
			}
			if(level>0) {
				int l = level-1;
				int h = g2d.getFontMetrics().getAscent()/2;
				g2d.drawLine(offsetX + marginX + l*levelIndentation+levelIndentation/2, y0, offsetX + marginX + l*levelIndentation+levelIndentation/2, y0+rowHeight);
				g2d.drawLine(offsetX + marginX + l*levelIndentation+levelIndentation/2, y0+h, offsetX + marginX + l*levelIndentation+levelIndentation, y0+h);
			}
			if(tree.children!=null) {
				for(TreeNode child : tree.children) {
					int d = recursivePaint(g2d, size, child, level+1, startRow+dodano);
					dodano += d;
				}
			}
			return dodano;
		}
		
	}

	private class ElementWrapperListModel extends AbstractListModel {

		private static final long serialVersionUID = 1L;

		@Override
		public Object getElementAt(int index) {
			return elementWrappers.get(index);
		}
		@Override
		public int getSize() {
			return elementWrappers.size();
		}
		
		public void elementStatusChanged(Object element) {
			int index = elementWrappers.indexOf(element);
			fireContentsChanged(this, index, index);
		}
		
		public void fireUpdatedAll() {
			if(elementWrappers.size()==0) return;
			fireContentsChanged(this, 0, elementWrappers.size()-1);
		}
	}
	
	private static class ElementWrapper {
		Element element;
		boolean used;
		public ElementWrapper(Element e) {
			element = e;
		}
		@Override
		public String toString() {
			String text = element.shortName + "("+element.kind+")";
			if(!used) {
				text += " - nije dodan";
			}
			return text;
		}
	}
	
	private static class Element {
		String kind;
		Long id;
		String shortName;
		String name;
		boolean invalid;
		
		@Override
		public String toString() {
			return shortName + "("+kind+")";
		}
	}
	
	private static class TreeNode {
		List<TreeNode> children = new ArrayList<TreeNode>();
		Element element;
		TreeNode parent;
		
		public void addChild(TreeNode node) {
			if(node.parent!=null) {
				node.parent.removeChild(node);
			}
			node.parent = this;
			children.add(node);
		}
		
		public void addAbove(TreeNode node) {
			if(node.parent!=null) {
				node.parent.removeChild(node);
			}
			node.parent = this.parent;
			this.parent.children.add(this.parent.children.indexOf(this), node);
		}
		
		public boolean moveUp() {
			if(this.parent==null) {
				return false;
			}
			int index = this.parent.children.indexOf(this);
			if(index<1) return false;
			TreeNode other = this.parent.children.get(index-1);
			this.parent.children.set(index, other);
			this.parent.children.set(index-1, this);
			return true;
		}
		
		public boolean deIndent() {
			if(this.parent==null || this.parent.parent==null) {
				return false;
			}
			int parentIndex = this.parent.parent.children.indexOf(this.parent);
			TreeNode myParent = this.parent;
			myParent.removeChild(this);
			myParent.parent.children.add(parentIndex+1, this);
			this.parent = myParent.parent;
			return true;
		}
		
		public boolean indent() {
			if(this.parent==null || this.parent==null) {
				return false;
			}
			int myIndex = this.parent.children.indexOf(this);
			if(myIndex<=0) return false;
			TreeNode noviRoditelj = this.parent.children.get(myIndex-1);
			this.parent.removeChild(this);
			noviRoditelj.addChild(this);
			return true;
		}
		
		public boolean delete() {
			if(this.parent==null || this.parent==null) {
				return false;
			}
			this.parent.removeChild(this);
			return true;
		}
		
		public boolean moveDown() {
			if(this.parent==null) {
				return false;
			}
			int index = this.parent.children.indexOf(this);
			if(index>=this.parent.children.size()-1) return false;
			TreeNode other = this.parent.children.get(index+1);
			this.parent.children.set(index, other);
			this.parent.children.set(index+1, this);
			return true;
		}
		
		public void addBelow(TreeNode node) {
			if(node.parent!=null) {
				node.parent.removeChild(node);
			}
			node.parent = this.parent;
			this.parent.children.add(this.parent.children.indexOf(this)+1, node);
		}
		
		public void removeChild(TreeNode node) {
			int pos = children.indexOf(node);
			if(pos==-1) return;
			node.parent = null;
			children.remove(pos);
		}
		
		public boolean addBelowSupported() {
			return parent!=null;
		}
		
		public boolean addAboveSupported() {
			return parent!=null;
		}
	}
	
	private static class RenderedTreeNode {
		TreeNode node;
		int x0;
		int y0;
		int x1;
		int y1;
		
		public RenderedTreeNode(TreeNode node, int x0, int y0, int x1, int y1) {
			super();
			this.node = node;
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
		}

		public boolean containsPoint(int x, int y) {
			if(x < x0) return false;
			if(x > x1) return false;
			if(y < y0) return false;
			if(y > y1) return false;
			return true;
		}
	}
}
