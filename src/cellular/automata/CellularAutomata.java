
package cellular.automata;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;


public class CellularAutomata extends JFrame {
    
    public static int windowSize = 800;
    public static int cellSize = 20;
    public static boolean firstPaint = true;
    int blue = 255;

    // initial 2D arrays
    public static int cellShadeValue[][] = new int[windowSize/cellSize][windowSize/cellSize];
    int cellShadeValueNext[][] = new int[windowSize/cellSize][windowSize/cellSize];
    int cellShadeValuePast[][] = new int[windowSize/cellSize][windowSize/cellSize];
    
    
    // setting the first generation
    public static void fillFirstMoment() {
        
        for (int i = 0; i < cellShadeValue.length; i++) {
            for (int j = 0; j < cellShadeValue[0].length; j++) {               
                cellShadeValue[i][j] = 255;     // setting all cells to the main shade of blue
            }                                   // the int value stands for amount of green, red can then be calculated
        }
    }
    
    
    // setting the next generation
    public void fillNextMoment() {
 
        for (int i = 0; i < cellShadeValue.length; i++) {
            for (int j = 0; j < cellShadeValue[0].length; j++) {
                // evolution rules
                int nextShadeRough = calculatingNeighbours(i,j);    // "sum" of shades around it (sumNeighbour)
                
                // if all neighbours are main blue
                if (nextShadeRough == 255) {
                    cellShadeValueNext[i][j] = 255;
                }
                // otherwise, calculate the next shade
                else {
                    cellShadeValueNext[i][j] = nextShadeRough;
                }
            }
        }

        // moving info in cellShadeValue to cellShadeValuePast
        for (int i = 0; i < cellShadeValue.length; i++) {
            for (int j = 0; j < cellShadeValue[0].length; j++) {
                cellShadeValuePast[i][j] = cellShadeValue[i][j];
            }
        }
        
        // moving info in cellShadeValueNext to cellShadeValue
        for (int i = 0; i < cellShadeValue.length; i++) {
            for (int j = 0; j < cellShadeValue[0].length; j++) {
                cellShadeValue[i][j] = cellShadeValueNext[i][j];
            }
        }
        
         // generating raindrops
        Random r = new Random();
        int rainChance = r.nextInt(10);
        if (rainChance < 5) {
            int i = r.nextInt(windowSize/cellSize);
            int j = r.nextInt(windowSize/cellSize);
            cellShadeValue[i][j] = 70;  // setting raindrops to the darkest shade of blue
        }
    }
    

    // calculating the shade of blue in neighbours
    public int calculatingNeighbours(int i, int j) {
        
        // accounting for the limits of the window
        int minCheckA = -1;
        int maxCheckA = 1;
        int minCheckB = -1;
        int maxCheckB = 1;        
        
        int sumNeighbour = 255;
        int temp;
     
        // checking for any cells on the boundary, which would have less neighbours
        if (i == 0) {
            minCheckA = 0;
        }
        if (j == 0) {
            minCheckB = 0;
        }
        if (i == windowSize/cellSize - 1) {
            maxCheckA = 0;
        }
        if (j == windowSize/cellSize - 1) {
            maxCheckB = 0;
        }
        
        int numNeighbours = 4;
        
        if (minCheckA == 0) {
            numNeighbours -= 1;
        }
        if (minCheckB == 0) {
            numNeighbours -= 1;
        }
        if (maxCheckA == 0) {
            numNeighbours -= 1;
        }
        if (maxCheckB == 0) {
            numNeighbours -= 1;
        }
        
        // creating a new array for neighbours that are not the main shade of blue
        ArrayList<String> neighboursNeeded = new ArrayList<String>();
        
        for (int a = minCheckA; a <= maxCheckA; a++) {
            for (int b = minCheckB; b <= maxCheckB; b++) {
                temp = cellShadeValue[i+a][j+b];
                if ((a==0 || b==0) && (a!=0 || b!=0) && temp != 255) {
                    String tempString = Integer.toString(temp);
                    neighboursNeeded.add(tempString);
                }
            }
        }
        
        if (neighboursNeeded.size() > 0) {      // checking that there are non-main blue neighbours
            int shade = Integer.parseInt(neighboursNeeded.get(0));

            // looking for doubles of the same shade
            int numDuplicate = 1;
            
            for (int k = 1; k < neighboursNeeded.size(); k++) {
                if (Integer.parseInt(neighboursNeeded.get(k)) == shade) {
                    numDuplicate ++;
                }
            }
            
            // if a cell is surrounded by the same shade, it's most likely already been a part of it
            if (numDuplicate > 2) {
                sumNeighbour = 255;
            }
            // if there are 2 of the same shaded neighbours, they could either be from a past ripple or a new one
            else if (numDuplicate == 2) {
                //calculate using past shade
                int shade2 = (255 - cellShadeValuePast[i][j])/2 + cellShadeValuePast[i][j]; ////////////////////////////
                if (shade2 == shade) {
                    
                    //set to 255 (has already been part of this ripple)
                    sumNeighbour = 255;
                }
                //otherwise, take the shade and calculate like normal
                else {
                    return (cellShadeValue[i][j] - shade) / 2 + shade;
                }
            }
            // else, add up the different neighbours for the new shade
            else {
                int addedNeighbours = 0;
                for (int k = 0; k < neighboursNeeded.size(); k++) {
                    addedNeighbours += Integer.parseInt(neighboursNeeded.get(k));
                }
                int x = addedNeighbours/neighboursNeeded.size();
                sumNeighbour = (255 - x)/2 + x;
            }
        }
        return sumNeighbour;
    }
    
    
    public Image getImage() {
        
        BufferedImage bi = new BufferedImage(windowSize,windowSize, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        
        // the rules apply to any generation except first
        if (firstPaint == false) {
            fillNextMoment();  // where the rules are
        }
        
        // setting colours and drawing each cell
        for (int i = 0; i < cellShadeValue.length; i++) {
            for (int j = 0; j < cellShadeValue[0].length; j++) {
                int green = cellShadeValue[i][j];
                int red = cellShadeValue[i][j] - 70;
                Color c = new Color(red,green,blue);
                g2.setColor(c);
                g2.fillRect(j*cellSize, i*cellSize, cellSize, cellSize);
            }
        }
        
        // drawing the grid last so it appears on top of the cells
        g2.setColor(Color.GRAY);
        for (int i = 0; i < windowSize ; i += cellSize) {
            g2.drawLine(i, 0, i, windowSize);
            g2.drawLine(0, i, windowSize, i);
        }
        return bi;
    }
    
    public void paint(Graphics g) {
        
        Image img = getImage();
        g.drawImage(img, 0, 0, rootPane);
    }
    
    
    public void sleep(int numMilliseconds) {
        try {
            Thread.sleep(numMilliseconds);
        }
        catch (Exception e) {
        }
    }


    public static void main(String[] args) throws IOException {
        
        CellularAutomata myWindow = new CellularAutomata();
        
        fillFirstMoment();

        myWindow.setTitle("Ripple Effect");
        myWindow.setSize(windowSize,windowSize);
        myWindow.setBackground(Color.WHITE);
        myWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
        myWindow.setVisible(true);
                
        // main loop
        while (true) {
            myWindow.repaint();
            myWindow.sleep(150);
            firstPaint = false;
        }
    }
}