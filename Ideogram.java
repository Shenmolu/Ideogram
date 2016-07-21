import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import de.erichseifert.vectorgraphics2d.PDFGraphics2D;

import java.awt.BasicStroke;
// Graphic Libraries
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class Ideogram {
	
	private double canvasWidth;		// Canvas width
	private double canvasHeight;	// Canvas height
	private double width;			// Width of a chromosome band
	private double scaleRate;		// Scale factor
	private double dx;				// Offset from left
	private double dy, dy2;			// Offsets from top
	private double space;
    private boolean showRuler;
	
    // Constructor
    public Ideogram() {
    	canvasWidth = 240;
    	canvasHeight = 180;
    	width = 5;
    	scaleRate = canvasHeight/324000000;
    	dx = 12.5;
    	dy = 12.5;
    	dy2 = canvasHeight*2/3;
    	showRuler = false;
    	space = (canvasWidth - 2*dx - width)/15;
    }
	
    // Setters
	public void setCanvasWidth(double d) {
		canvasWidth = d;
		space = (canvasWidth - 2*dx - width)/15;
	}
	public void setCanvasHeight(double d) {
		canvasHeight = d;
    	scaleRate =  canvasHeight/324000000;
    	dy2 = canvasHeight*2/3;
	}
	public void setChromWidth(double d) {
		width = d;
		space = (canvasWidth - 2*dx - width)/15;
	}
	public void setScaleRate(double d) {
		scaleRate = canvasHeight/d;
	}
	public void setLeftOffset(double d) {
		dx = d;
		space = (canvasWidth - 2*dx - width)/15;
	}
	public void setTopOffset(double d) {
		dy = d;
	}
	public void showRuler(boolean b) {
		showRuler = b;
	}
	private void clipRect(Chromosome c, int start, int end, Color color, Graphics2D g) {
		Rectangle2D.Double r;
		if (c.index < 16) {
			r = new Rectangle2D.Double(dx + c.index*space,
					dy + start*scaleRate, width, (end - start)*scaleRate);
		}
		else {
			r = new Rectangle2D.Double(dx + (c.index-8)*space,
					dy2 + start*scaleRate, width, (end - start)*scaleRate);
		}
				
		g.setClip(r);
		drawChromosome(c, true, color, g);
		g.setClip(null);
	}
	private void drawChromosome(Chromosome c, boolean fill, Color color, Graphics2D g) {
		RoundRectangle2D.Double p;
		RoundRectangle2D.Double q;
		int x;
		int y;
		double space = (canvasWidth - 2*dx - width)/15;
		if (c.index < 16) {
			p = new RoundRectangle2D.Double(dx + c.index*space,
					dy, width, c.centromere*scaleRate, 3, 4);
			q = new RoundRectangle2D.Double(dx + c.index*space,
					dy + c.centromere*scaleRate, width,
					c.length*scaleRate - c.centromere*scaleRate, 3, 4);
			x = (int) (dx + c.index*space);
			y = (int) (dy*2/3);
			
			if (showRuler == true) {
				drawRuler(dx + c.index*space - 1, dy, c.length, g);
			}
	    }
		else {
			p = new RoundRectangle2D.Double(dx + (c.index-8)*space,
					dy2, width, c.centromere*scaleRate, 3, 4);
			q = new RoundRectangle2D.Double(dx + (c.index-8)*space,
					dy2 + c.centromere*scaleRate, width,
					c.length*scaleRate - c.centromere*scaleRate, 3, 4);
			x = (int) (dx + (c.index-8)*space);
			y = (int) (dy2 - dy/3);
			
			if (showRuler == true) {
				drawRuler(dx + (c.index-8)*space - 1, dy2, c.length, g);
			}
	     }
		
		if (fill) {
			g.setColor(color);
			g.fill(p);
			g.fill(q);
		}
		else {
			g.draw(p);
			g.draw(q);
			g.setFont(new Font("Arial", Font.PLAIN, 3));
			g.drawString(c.name, (float) x, (float) y);
		}
	}
	private void drawChromosomes(Chromosome[] chroms, Graphics2D g) {
		for (int i = 0; i < chroms.length; i++) {
			drawChromosome(chroms[i], false, Color.BLACK, g);
		}
	}
	private void drawColorBox(int x, int y, Color color, String s, Graphics2D g) {
		Rectangle r = new Rectangle(x, y, 3, 3);
		g.setColor(color);
		g.fill(r);
		g.setColor(Color.BLACK);
		g.draw(r);
		g.setFont(new Font("Arial", Font.PLAIN, 5));
		g.drawString(s, x + 6, y + 3);
	}
	private void drawIndication(int x, int y, Graphics2D g) {
		
		drawColorBox(x, y, Color.BLACK, "duplication/gain", g);
		drawColorBox(x, y + 6, Color.GRAY, "deletion/loss", g);
	}
	private void drawKaryotype (File file, Chromosome[] chroms, Graphics2D g) {
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new FileReader(file));

	            String line = null;
	            Color color = Color.BLACK;
	            while ((line = reader.readLine()) != null) {
	                String [] token = line.split("\\s+");

	                // Get type
	                String type = token[3];
	                if (type.equalsIgnoreCase("normal")) {
	                	continue;
	                }
	                else if (type.equalsIgnoreCase("duplication") || 
	                		type.equalsIgnoreCase("gain")) {
	                	color = Color.BLACK;
	                }
					else if (type.equalsIgnoreCase("deletion") ||
							type.equalsIgnoreCase("loss")) {
						color = Color.GRAY;
					}
	                
	                // Get name
	                int index;
	                String name = token[0].substring(3, token[0].length());
	                if (name.compareToIgnoreCase("x") == 0) {
	                    index = 15;
	                }
	                else if (name.compareToIgnoreCase("y") == 0) {
	                    index = 23;
	                }
	                else {
	                    index = Integer.parseInt(name);
	                    if (index < 16)
	                    	--index;
	                }
	                
	                int start = Integer.parseInt(token[1]);
	                int end = Integer.parseInt(token[2]);
	                
	                clipRect(chroms[index], start, end, color, g);
	            }
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
	    }
	private Chromosome[] readCytoband (File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            Chromosome[] chroms = new Chromosome[24];
            String line = null;
            int lastIndex = 0;
            double lastEnd = 0.0;
            boolean top = true;
            boolean shortArm = true;
            while ((line = reader.readLine()) != null) {
                String [] token = line.split("\\s+");

                // Get name
                int index;
                String name = token[0].substring(3, token[0].length());
                if (name.compareToIgnoreCase("x") == 0) {
                    index = 15;
                }
                else if (name.compareToIgnoreCase("y") == 0) {
                    index = 23;
                }
                else {
                    index = Integer.parseInt(name);
                    if (index < 16)
                    	--index;
                }
                if (chroms[index] == null) {
                	chroms[index] = new Chromosome(token[0], index);
                }


                // Get centromere location
                char arm = token[3].charAt(0);
                if (arm == 'q' && shortArm) {
                    chroms[index].centromere = lastEnd;
                    shortArm = false;
                }

                // Get length
                int end = Integer.parseInt(token[2]);
                if (index != lastIndex && top == false) {
                    chroms[lastIndex].length = lastEnd;
                    lastIndex = index;
                    shortArm = true;
                }
                lastEnd = end;
                top = false;
            }
            chroms[lastIndex].length = lastEnd;
            reader.close();
            return chroms;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
		return null;
    }
	private void drawRuler(double x, double y, double length, Graphics2D g) {
		Line2D.Double l = new Line2D.Double(x, y, x, y + length*scaleRate);
		g.draw(l);
		double scale = 20000000*scaleRate;
		for(int i = 0; i < length/20000000; i++) {
			Line2D.Double m = new Line2D.Double(x-1, y + i*scale, x, y + i*scale);
			g.draw(m);
			g.setFont(new Font("Arial", Font.PLAIN, 2));
			int s;
			if (i == 0) {
				s = 0;
			} else if (i <5) {
				s = 1;
			} else {
				s = 2;
			}
			g.drawString(String.valueOf(i*20), (float) (x-2.5-s), (float) (y + i*scale + 0.5));
		}
		
		
	}
	

	public void createIdeogram (File cytoband, File karyotype, String path) throws IOException {
		PDFGraphics2D g = new PDFGraphics2D(0.0, 0.0, canvasWidth, canvasHeight);
        g.setStroke(new BasicStroke(0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        
        // Read cytobands
    	Chromosome[] chroms = readCytoband(cytoband);
    	
        // Mark karyotypes
    	drawKaryotype(karyotype, chroms, g);
    	
    	// Draw chromosomes
    	g.setColor(Color.BLACK);
    	drawChromosomes(chroms, g);
        
        // Draw indications
        drawIndication((int) (canvasWidth*1/4), (int) (canvasHeight*5/6), g);
        // Write the PDF output to a file
        FileOutputStream out = new FileOutputStream(path);
        try {
            out.write(g.getBytes());
        } finally {
            out.close();
        }
	}	

	public static void main(String arg[]) throws IOException {
		File c = new File(arg[0]);
		File k = new File(arg[1]);
		String o = arg[2];
		Ideogram i = new Ideogram();
		i.createIdeogram(c, k, o);
	}
}

class Chromosome {
    String name;
    int index;
    double length;
    double centromere;
    
    Chromosome(String n, int i) {
    	name = n;
    	index = i;
    	length = 0;
    	centromere = 0;
    }
}
