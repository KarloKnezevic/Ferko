package hr.fer.zemris.jcms.service2.course.wiki;

import hr.fer.zemris.jcms.beans.wiki1.ExternalProblemsListWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.HeadingWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.LinkWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.ListItemWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.ListWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.ListWikiType;
import hr.fer.zemris.jcms.beans.wiki1.ParagraphWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.StyleWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.StyleWikiType;
import hr.fer.zemris.jcms.beans.wiki1.TextWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.WikiNode;
import hr.fer.zemris.jcms.beans.wiki1.WikiNodeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pomoćni razred s metodama za rad s wikijem.
 * 
 * @author marcupic
 */
public class CourseWikiUtil {

	/**
	 * Metoda prima <code>pageURL</code> i vraća njegovu dekompoziciju na dijelove.
	 * Ako je <code>pageURL</code> jednak <code>null</code> ili ako je prazan, vraća 
	 * se prazna lista. U suprotnom radi se splitanje po znaku '/'. Pri tome se dva
	 * uzastopna znaka '/' tretiraju kao jedan escapeani '/' po kojem se ne splita,
	 * već se taj jedan '/' propušta dalje kao sastavni dio komponente.
	 * <p>Važno: vraćena lista je neizmjenjiva!</p>
	 * @param pageURL URL koji treba razdijeliti
	 * @return lista dijelova
	 */
	public static List<String> parsePageURL(String pageURL) {
		// Ako je null ili prazan...
		if(pageURL==null || pageURL.isEmpty()) return Collections.unmodifiableList(new ArrayList<String>());
		// Inače parsiraj...
		char[] data = pageURL.toCharArray();
		List<String> result = new ArrayList<String>();
		int curr = 0;
		int lastValid = 0;
		while(curr < data.length) {
			// Ako nije razdjelnik:
			if(data[curr]!='/') {
				data[lastValid] = data[curr];
				lastValid++;
				curr++;
				continue;
			}
			// Ako je escapeani razdjelnik:
			if(curr+1<data.length && data[curr+1]=='/') {
				data[lastValid] = data[curr];
				lastValid++;
				curr++;
				curr++;
				continue;
			}
			// Inače je razdjelnik
			result.add(new String(data, 0, lastValid));
			curr++;
			lastValid = curr;
			lastValid = 0;
		}
		// Dodaj i zadnju komponentu (odnosno prazan string ako je staza završavala
		// znakom /
		result.add(new String(data, 0, lastValid));
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Metoda gradi pageURL iz predanih dijelova. U slučaju da je neki dio <code>null</code>,
	 * tretirat će se kao prazna komponenta. Ako se u komponenti nalazi znak '/', metoda će
	 * ga automatski escapeati.
	 * 
	 * @param parts
	 * @return
	 */
	public static String buildPageURL(String ... parts) {
		int totalLen = 0;
		for(String part : parts) {
			totalLen += part.length();
		}
		StringBuilder sb = new StringBuilder((int)(1.1*totalLen));
		boolean prvi = true;
		for(String part : parts) {
			if(prvi) {
				prvi = false;
			} else {
				sb.append('/');
			}
			if(part==null || part.isEmpty()) {
				continue;
			}
			char[] data = part.toCharArray();
			for(char c : data) {
				sb.append(c);
				if(c=='/') {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}
	
	public static WikiNode parseWiki(String text) {
		return new Wiki1Parser(text).getTree();
	}
	
	private static enum StartMarker {
		INVALID,
		LIST_UNNUMBERED,
		LIST_ALPHA,
		LIST_NUMBERED,
		HEADING
	}
	
	private static class Wiki1Parser {
		
		private WikiNode tree;
		private char[] data;
		private char[] copy;
		private List<WikiNode> stack = new ArrayList<WikiNode>();
		private int end;
		private int cur;
		private boolean noMoreLines;
		private StartMarker lineType;
		private int lineIndent;
		private int headingLevel;
		private String numberedListType;
		
		public Wiki1Parser(String text) {
			data = text.toCharArray();
			copy = new char[data.length+1];
			parse();
		}
		
		private void parse() {
			WikiNode root = new WikiNode(WikiNodeType.ROOT_ELEMENT);
			tree = root;
			stack.add(root);
			nextLine();
ou:			while(!noMoreLines) {
				// Ako je redak prazan, zatvori sve strukture...
				if(cur==0) {
					while(stack.size()>1) {
						stack.remove(stack.size()-1);
					}
					nextLine();
					continue;
				}
				prepoznajPocetak();
				if(lineType==StartMarker.INVALID) {
					WikiNode top = stack.get(stack.size()-1);
					
					// Ako sam u paragrafu...
					if(top.getNodeType()==WikiNodeType.PARAGRAPH) {
						// Dodaj mu ovo kao djecu
						parseLine();
						nextLine();
						continue ou;
					}
					
					// Mozda je nastavak na item prethodne liste?
					WikiNode n = top;
					while(n.getParent()!=null) {
						WikiNode c = n;
						n = n.getParent();
						if(c.getNodeType()==WikiNodeType.LISTITEM) {
							ListWikiNode ln = (ListWikiNode)c.getParent();
							if(ln.getIndentation() == lineIndent) {
								n = top;
								while(n != c) {
									n = n.getParent();
									stack.remove(stack.size()-1);
								}
								top = n;
								// Dodaj ovaj element ovom itemu; zatvori eventualno sve ispod njega...
								parseLine();
								nextLine();
								continue ou;
							}
						}
					}
					
					// Ako nisam u paragrafu, i nije nastavak na neki item liste, zapocni novi paragraf...
					while(stack.size()>1) {
						stack.remove(stack.size()-1);
					}
					top = new ParagraphWikiNode(stack.get(0));
					stack.add(top);
					// Dodaj ovaj u top...
					parseLine();
					nextLine();
					continue ou;
				}
				if(lineType==StartMarker.HEADING) {
					while(stack.size()>1) {
						stack.remove(stack.size()-1);
					}
					WikiNode top = new HeadingWikiNode(stack.get(0), headingLevel);
					stack.add(top);
					int cur2 = cur-1;
					while(cur2>lineIndent && copy[cur2]==' ') { cur = cur2; cur2--; }
					while(cur2>lineIndent && copy[cur2]=='=') { cur = cur2; cur2--; }
					// Dodaj ovaj u top...
					parseLine();
					// I zatim makni heading sa stoga...
					while(stack.size()>1 && stack.get(stack.size()-1)!=top.getParent()) {
						stack.remove(stack.size()-1);
					}
					nextLine();
					continue ou;
				}
				if(lineType==StartMarker.LIST_ALPHA || lineType==StartMarker.LIST_NUMBERED || lineType==StartMarker.LIST_UNNUMBERED) {
					while(stack.size()>1) {
						WikiNode top = stack.get(stack.size()-1);
						// Ako gledam jedan listitem
						if(top.getNodeType()==WikiNodeType.LISTITEM) {
							ListWikiNode lwn = (ListWikiNode)top.getParent();
							// ako je njegova indentacija veća od moje, zatvori ga
							if(lwn.getIndentation()>lineIndent) {
								stack.remove(stack.size()-1);
								continue;
							}
							// Ako smo na istoj indentaciji, i iste smo vrste, zatvori njega, otvori mene, i dodaj unutra
							if(lwn.getIndentation()==lineIndent && ((lineType==StartMarker.LIST_ALPHA && lwn.getListType()==ListWikiType.ALPHA)||(lineType==StartMarker.LIST_NUMBERED && lwn.getListType()==ListWikiType.NUMBERED)||(lineType==StartMarker.LIST_UNNUMBERED && lwn.getListType()==ListWikiType.UNNUMBERED)) ) {
								stack.remove(stack.size()-1);
								top = new ListItemWikiNode(lwn);
								stack.add(top);
								// Dodaj ovaj u top...
								parseLine();
								nextLine();
								continue ou;
							}
							// Ako smo na istoj indentaciji, i različite smo vrste, zatvori njega i njegovu listu, otvori moju listu i mene, i dodaj unutra
							if(lwn.getIndentation()==lineIndent && ((lineType==StartMarker.LIST_ALPHA && lwn.getListType()==ListWikiType.ALPHA)||(lineType==StartMarker.LIST_NUMBERED && lwn.getListType()==ListWikiType.NUMBERED)||(lineType==StartMarker.LIST_UNNUMBERED && lwn.getListType()==ListWikiType.UNNUMBERED)) ) {
								stack.remove(stack.size()-1);
								stack.remove(stack.size()-1);
								top = new ListWikiNode(lwn.getParent(),lineType==StartMarker.LIST_ALPHA?ListWikiType.ALPHA:(lineType==StartMarker.LIST_NUMBERED?ListWikiType.NUMBERED:ListWikiType.UNNUMBERED),lineIndent, numberedListType);
								stack.add(top);
								top = new ListItemWikiNode(top);
								stack.add(top);
								// Dodaj ovaj u top...
								parseLine();
								nextLine();
								continue ou;
							}
							// Ako je njegova indentacija manja od moje, otvori moju listu i mene, i dodaj unutra
							if(lwn.getIndentation()<lineIndent) {
								top = new ListWikiNode(top,lineType==StartMarker.LIST_ALPHA?ListWikiType.ALPHA:(lineType==StartMarker.LIST_NUMBERED?ListWikiType.NUMBERED:ListWikiType.UNNUMBERED),lineIndent, numberedListType);
								stack.add(top);
								top = new ListItemWikiNode(top);
								stack.add(top);
								// Dodaj ovaj u top...
								parseLine();
								nextLine();
								continue ou;
							}
							stack.remove(stack.size()-1);
						} else {
							stack.remove(stack.size()-1);
						}
					}
					// Ako sam dosao do ovog mjesta, gledam korijen; trebam dodati novu listu i sebe
					WikiNode top = stack.get(0);
					top = new ListWikiNode(top,lineType==StartMarker.LIST_ALPHA?ListWikiType.ALPHA:(lineType==StartMarker.LIST_NUMBERED?ListWikiType.NUMBERED:ListWikiType.UNNUMBERED),lineIndent, numberedListType);
					stack.add(top);
					top = new ListItemWikiNode(top);
					stack.add(top);
					// Dodaj ovaj u top...
					parseLine();
					nextLine();
					continue ou;
				}
				System.out.println("Ovdje nismo smjeli doći!!!");
			}
		}

		private void parseLine() {
			// Gledam [lineIndent, cur-1]
			copy[cur] = 0;
			WikiNode top = stack.get(stack.size()-1);
			WikiNode masterTop = top;
			int poc = lineIndent;
			int e = poc;
			int c = poc;
			int repCount;
			boolean deletable = false;
			StyleWikiType t = null;
ou:			while(c < cur) {
				switch(copy[c]) {
				case '!':
					copy[e] = copy[c];
					e++; c++;
					deletable = true;
					break;
				case '\'': 
					repCount = countRepeats(copy, c);
					if(repCount!=2 && repCount!=3 && repCount!=5 || deletable) {
						if(deletable) e--;
						for(int q = 0; q < repCount; q++) {
							copy[e] = copy[c];
							e++; c++;
						}
						deletable = false;
						continue;
					}
					deletable = false;
					// Tekst prije trebam dodati u top tag:
					if(e>poc) {
						new TextWikiNode(top, new String(copy, poc, e-poc));
					}
					c += repCount;
					// Da li je ovo tag koji zatvara ili otvara?
					t = repCount==2 ? StyleWikiType.ITALIC : ( repCount == 3 ? StyleWikiType.BOLD : StyleWikiType.BOLD_ITALIC);
					top = openOrCloseStyle(t, masterTop);
					e = poc;
					break;
				case '*': 
					repCount = countRepeats(copy, c);
					if(repCount!=2 || deletable) {
						if(deletable) e--;
						for(int q = 0; q < repCount; q++) {
							copy[e] = copy[c];
							e++; c++;
						}
						deletable = false;
						continue;
					}
					deletable = false;
					// Tekst prije trebam dodati u top tag:
					if(e>poc) {
						new TextWikiNode(top, new String(copy, poc, e-poc));
					}
					c += repCount;
					// Da li je ovo tag koji zatvara ili otvara?
					t = StyleWikiType.BOLD;
					top = openOrCloseStyle(t, masterTop);
					e = poc;
					break;
				case '~': 
					repCount = countRepeats(copy, c);
					if(repCount!=2 || deletable) {
						if(deletable) e--;
						for(int q = 0; q < repCount; q++) {
							copy[e] = copy[c];
							e++; c++;
						}
						deletable = false;
						continue;
					}
					deletable = false;
					// Tekst prije trebam dodati u top tag:
					if(e>poc) {
						new TextWikiNode(top, new String(copy, poc, e-poc));
					}
					c += repCount;
					// Da li je ovo tag koji zatvara ili otvara?
					t = StyleWikiType.STRIKETHROUGH;
					top = openOrCloseStyle(t, masterTop);
					e = poc;
					break;
				case '_': 
					repCount = countRepeats(copy, c);
					if(repCount!=2 || deletable) {
						if(deletable) e--;
						for(int q = 0; q < repCount; q++) {
							copy[e] = copy[c];
							e++; c++;
						}
						deletable = false;
						continue;
					}
					deletable = false;
					// Tekst prije trebam dodati u top tag:
					if(e>poc) {
						new TextWikiNode(top, new String(copy, poc, e-poc));
					}
					c += repCount;
					// Da li je ovo tag koji zatvara ili otvara?
					t = StyleWikiType.UNDERLINE;
					top = openOrCloseStyle(t, masterTop);
					e = poc;
					break;
				case '[':
					if(copy[c+1]=='[' && copy[c+2]=='/') {
						if(deletable) {
							e--;
							for(int q = 0; q < 3; q++) {
								copy[e] = copy[c];
								e++; c++;
							}
							deletable = false;
							break;
						}
						deletable = false;
						if(e>poc) {
							new TextWikiNode(top, new String(copy, poc, e-poc));
						}
						e = poc;
						// Imam tag!
						c += 3;
						int tagNameStart = c;
						while(copy[c]!=' ' && copy[c]!=']' && copy[c]!=0) {
							c++;
						}
						String tagName = new String(copy, tagNameStart, c-tagNameStart);
						if(copy[c+1]!=']') {
							break ou; // Imamo gresku u sintaksi!
						}
						c+=2;
						// Sada ga nadi i zatvori
						WikiNode n = top;
						WikiNodeType requestedType = tagName.equals("link") ? WikiNodeType.LINK : (tagName.equals("external-problems-list") ? WikiNodeType.EXTERNAL_PROBLEMS_LIST : WikiNodeType.LINK);
						while(n != masterTop) {
							if(n.getNodeType()!=requestedType) {
								n = n.getParent();
								stack.remove(stack.size()-1);
								continue;
							}
							stack.remove(stack.size()-1);
							break;
						}
						top = stack.get(stack.size()-1);
						break;
					}
					if(copy[c+1]=='[' && Character.isJavaIdentifierStart(copy[c+2])) {
						if(deletable) {
							e--;
							for(int q = 0; q < 3; q++) {
								copy[e] = copy[c];
								e++; c++;
							}
							deletable = false;
							break;
						}
						deletable = false;
						if(e>poc) {
							new TextWikiNode(top, new String(copy, poc, e-poc));
						}
						e = poc;
						// Imam jednostavan tag! Idemo vidjeti koji. To bi mogao biti npr.
						// [[wiki:a/b/c|Ovo je naziv]]
						// Ako nema |, a/b/c ujedno postaje i tekst koji se prikazuje
						c += 2;
						int tagNameStart = c;
						int pipePos = -1;
						while(copy[c]!=']' && copy[c]!=0) {
							if(copy[c]=='|') pipePos=c;
							c++;
						}
						if(copy[c]!=']' || copy[c+1]!=']') {
							break ou; // Imamo gresku u sintaksi!
						}
						String tagPart = pipePos!=-1 ? new String(copy, tagNameStart, pipePos-tagNameStart) : new String(copy, tagNameStart, c-tagNameStart);
						int dvotocka = -1;
						int traziDvotockuDo = pipePos!=-1 ? pipePos : c;
						for(int q = tagNameStart; q<traziDvotockuDo; q++) {
							if(copy[q]=='/') break;
							if(copy[q]==' ') break;
							if(copy[q]==':') {
								dvotocka = q;
								break;
							}
						}
						String tagPrefix = dvotocka==-1 ? null : tagPart.substring(0, dvotocka-tagNameStart);
						String tagName   = dvotocka==-1 ? tagPart : tagPart.substring(dvotocka-tagNameStart+1);
						String tagValue  = pipePos==-1  ? null : new String(copy, pipePos+1, c-pipePos-1);
						c+=2;

						if(tagPrefix==null) {
							tagPrefix = "wiki";
						}
						
						// Ako je wiki link
						if(tagPrefix.equals("wiki")) {
							if(tagValue == null) tagValue = tagName;
							Map<String,String> attributes = new HashMap<String, String>();
							attributes.put("type", "page");
							attributes.put("url", tagName);
							attributes.put("info", tagValue);
							new LinkWikiNode(top, attributes);
						} else if(tagPrefix.equals("url")) {
							if(tagValue == null) tagValue = tagName;
							Map<String,String> attributes = new HashMap<String, String>();
							attributes.put("type", "url");
							attributes.put("url", tagName);
							attributes.put("info", tagValue);
							new LinkWikiNode(top, attributes);
						} else {
							// Trenutno ignoriramo jer ne podržavamo druge vrste ovog tipa linkova...
						}
						break;
					}
					if(copy[c+1]=='[' && copy[c+2]=='@') {
						if(deletable) {
							e--;
							for(int q = 0; q < 3; q++) {
								copy[e] = copy[c];
								e++; c++;
							}
							deletable = false;
							break;
						}
						deletable = false;
						if(e>poc) {
							new TextWikiNode(top, new String(copy, poc, e-poc));
						}
						e = poc;
						// Imam tag!
						c += 3;
						int tagNameStart = c;
						while(copy[c]!=' ' && copy[c]!=']' && copy[c]!=0) {
							c++;
						}
						String tagName = new String(copy, tagNameStart, c-tagNameStart);
						// Citanje atributa
						Map<String, String> atributi = new HashMap<String, String>();
						while(true) {
							while(copy[c]==' ') c++;
							int astart = c;
							if(copy[astart]==0) break ou;
							if(copy[astart]==']') {
								if(copy[astart+1]!=']') {
									break ou; // Imamo gresku u sintaksi!
								}
								c += 2;
								break;  // Gotovi smo s atributima...
							}
							while(copy[c]!=' ' && copy[c]!='=' && copy[c]!=']' && copy[c]!=0) {
								c++;
							}
							String atribName = new String(copy, astart, c-astart);
							String atribValue = null;
							if(copy[c]=='=') {
								c++;
								if(copy[c]!='\"') {
									break ou; // Imamo gresku u sintaksi!
								}
								c++;
								astart = c;
								while(copy[c]!='\"' && copy[c]!=0) {
									c++;
								}
								atribValue = new String(copy, astart, c-astart);
								if(copy[c]=='\"') {
									c++;
								}
							}
							atributi.put(atribName, atribValue);
						}
						if(tagName.equals("link")) {
							top = new LinkWikiNode(top, atributi);
						} else if(tagName.equals("external-problems-list")) {
							top = new ExternalProblemsListWikiNode(top, atributi);
						} else {
							top = new LinkWikiNode(top, atributi);
						}
						stack.add(top);
						break;
					}
					deletable = false;
					copy[e] = copy[c];
					e++; c++;
					break;
				default:
					deletable = false;
					copy[e] = copy[c];
					e++; c++;
					break;
				}
			}
			if(e>poc) {
				new TextWikiNode(top, new String(copy, poc, e-poc)+"\r\n");
			} else {
				new TextWikiNode(top, "\r\n");
			}
			top = stack.get(stack.size()-1);
			while(top != masterTop) {
				stack.remove(stack.size()-1);
				top = top.getParent();
			}
			//new TextWikiNode(top, new String(copy, lineIndent, cur-lineIndent)+"\r\n");
		}

		private WikiNode openOrCloseStyle(StyleWikiType t, WikiNode masterTop) {
			WikiNode top = stack.get(stack.size()-1);
			WikiNode n = top;
			WikiNode nodeToClose = null;
			while(n != masterTop) {
				if(n.getNodeType()==WikiNodeType.STYLE) {
					StyleWikiNode swn = (StyleWikiNode)n;
					if(swn.getStyleType()==t) {
						// Tada ovo treba zatvoriti!
						nodeToClose = n;
						break;
					}
				}
				n = n.getParent();
			}
			if(nodeToClose!=null) {
				n = top;
				while(n != nodeToClose) {
					n = n.getParent();
					stack.remove(stack.size()-1);
				}
				stack.remove(stack.size()-1);
				top = stack.get(stack.size()-1);
				return top;
			}
			// Inače ga otvaram:
			top = new StyleWikiNode(top, t);
			stack.add(top);
			return top;
		}
		
		private int countRepeats(char[] arr, int pos) {
			int p = pos+1;
			while(arr[p]==arr[pos]) p++;
			return p-pos;
		}

		private void prepoznajPocetak() {
			headingLevel = 0;
			lineIndent = 0;
			lineType = StartMarker.INVALID;
			int state = 0;
			boolean negate = false;
			for(int i = 0; i < cur; i++) {
				switch(state) {
				case 0:
					if(copy[i]==' ') { lineIndent = i+1; break; }
					if(copy[i]=='!') { state = 1; negate = true; break; }
					if(copy[i]=='1') { state = 2; numberedListType=null; break; }
					if(copy[i]=='a') { state = 5; numberedListType="a"; break; }
					if(copy[i]=='A') { state = 5; numberedListType="A"; break; }
					if(copy[i]=='i') { state = 5; numberedListType="i"; break; }
					if(copy[i]=='I') { state = 5; numberedListType="I"; break; }
					if(copy[i]=='*') { state = 8; break; }
					if(copy[i]=='=') { state = 11; headingLevel++; break; }
					return;
				case 1:
					if(copy[i]=='1') { state = 2; numberedListType=null; break; }
					if(copy[i]=='a') { state = 5; numberedListType="a"; break; }
					if(copy[i]=='A') { state = 5; numberedListType="A"; break; }
					if(copy[i]=='i') { state = 5; numberedListType="i"; break; }
					if(copy[i]=='I') { state = 5; numberedListType="I"; break; }
					if(copy[i]=='*') { state = 8; break; }
					if(copy[i]=='=') { state = 11; break; }
					return;
				case 2:
					if(copy[i]=='.') { state = 3; break; }
					return;
				case 3:
					if(copy[i]==' ') { break; }
					if(!negate) {
						lineType = StartMarker.LIST_NUMBERED;
						lineIndent = i;
					}
					return;
				case 5:
					if(copy[i]=='.') { state = 6; break; }
					return;
				case 6:
					if(copy[i]==' ') { break; }
					if(!negate) {
						lineType = StartMarker.LIST_ALPHA;
						lineIndent = i;
					}
					return;
				case 8:
					if(copy[i]==' ') { state = 9; break; }
					return;
				case 9:
					if(copy[i]==' ') { break; }
					if(!negate) {
						lineType = StartMarker.LIST_UNNUMBERED;
						lineIndent = i;
					}
					return;
				case 11:
					if(copy[i]=='=') { headingLevel++; break; }
					if(copy[i]==' ') { state = 12; break; }
					return;
				case 12:
					if(copy[i]==' ') { break; }
					if(!negate) {
						lineType = StartMarker.HEADING;
						lineIndent = i;
					}
					return;
				default:
					return;
				}
			}
		}

		private void nextLine() {
			if(noMoreLines) return;

			int pos = end;
			boolean shouldContinue;
			cur = 0;
			
			if(pos>=data.length) {
				noMoreLines = true;
				return;
			}
			
			try {
				do {
					shouldContinue = false;
					while(data[pos]!='\r' && data[pos]!='\n') {
						if(data[pos]=='\\' && (data[pos+1]=='\r' || data[pos+1]=='\n')) {
							shouldContinue = true;
							pos++;
							break;
						}
						copy[cur] = data[pos];
						pos++;
						cur++;
					}
					if(data[pos]=='\r' && data[pos+1]=='\n') {
						pos++;
					}
					pos++;
				} while(shouldContinue);
				end = pos;
			} catch(IndexOutOfBoundsException ex) {
				end = data.length;
			}
		}

		public WikiNode getTree() {
			return tree;
		}
	}

	// Test
	public static void main(String[] args) {
		String pageURL = buildPageURL("studtest2problem","list","http://studtest.zemris.fer.hr/problemGenerators#custom/zad_rg_001","variant1");
		System.out.println(pageURL);
		System.out.println(parsePageURL(pageURL));
		System.out.println(parsePageURL(pageURL+"/"));
		System.out.println(parsePageURL("external-problems/list/studtest2:http:////studtest.zemris.fer.hr//problemGenerators#custom//zad_rg_001//variant1"));
		String text = " = Naslov1=\r\n == Naslov2 ==\r\n\'\'\'\'\'Ovo\'\'\'\'\' je proba za [[@link type=\"studtest2problem\" url=\"http://studtest.zemris.fer.hr/problemGenerators#custom/zad_rg_001\" config=\"variant1\"]]link[[/link]].\r\nOvo je drugi redak istog paragrafa.\r\n\r\nOvo je drugi paragraf, redak 1.\r\nOvo je drugi paragraf, redak 2.\r\n\r\nOvo je treći paragraf.\r\n"+
		              " * buletirano 1\r\n" +
		              " * buletirano 2\r\n" +
		              "    1. podnumerirano 1\r\n" + 
		              "       Poruka\r\n" +
		              "    1. podnumerirano 2\r\n" + 
		              "       Poruka\r\n" +
		              "    a. slovno 1\r\n" + 
		              " * buletirano 3\r\n" +
		              "\r\n";

		Wiki1Parser parser = new Wiki1Parser(text);
		System.out.println(new HtmlWikiRenderer().render(parser.getTree()));
	}


}
