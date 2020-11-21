package uk.ac.ed.inf.aqmaps;

public class App 
{
		
    public static void main( String[] args ) {
        
//        Arg 0: day
//        Arg 1: month
//        Arg 2: year
//        Arg 3: starting point x
//        Arg 4: starting point y
//        Arg 5: seed
//        Arg 6: port
    	
//    	Map map = new Map(arg[0], arg[1], arg[2], arg[6]);
    	Map map = new Map("15", "06", "2021", "80");
    	map.renderGeojson();
    	
    }
}
