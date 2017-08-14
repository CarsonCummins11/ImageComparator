package Base_Engine;

import java.util.ArrayList;

public class Pixel {
	//Max difference between colors to be included in structure (decrease for more speed, less accuracy)
public final static double TOLERANCE = 23.0;
int row;
int column;
int red;
int green;
int blue;
int alpha;
ArrayList<Pixel> adjacent;
boolean inStruct = false;
	public Pixel(int Row, int Column, int Red, int Green, int Blue) {
		row = Row;
		column = Column;
		red = Red;
		green = Green;
		blue = Blue;
	}
	public ArrayList<Pixel> Build(Pixel p){
		int[] col = colAverage(p);
		inStruct = true;
		ArrayList<Pixel> ret = new ArrayList<Pixel>();
		ret.add(this);
		Pixel pNew = new Pixel(p.row,p.column,col[0],col[1],col[2]);
		for (int i = 0; i < adjacent.size(); i++) {
			if(adjacent.get(i).colDif(pNew)<TOLERANCE&&adjacent.get(i).inStruct==false){
			ret.addAll(adjacent.get(i).Build(pNew));
			}
			}
		return ret;
	}




	
private int[] colAverage(Pixel p){
	int[] ret = new int[3];
	ret[0] = (p.red+red)/2;
	ret[1] = (p.green+green)/2;
	ret[2] = (p.blue+blue)/2;
	return ret;
}
	private double colDif(Pixel p) {
		return Math.abs(((p.red-red)+(p.green-green)+(p.blue-blue))/3);
		
	}

}

