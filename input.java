//Stream<String> stream = Files.lines(Paths.get("robots.mat"));
//ArrayList<Map> maps = new ArrayList<>();
//String regexFloat = "(-?[.0-9]*)";
//for(Object line : stream.toArray()) {
//    Map newMap = new Map();
//    String currentLine = line.toString().replaceAll("\\s","");
//    String[] divideString = currentLine.split("#");
//
//    Matcher m = Pattern.compile("\\("+regexFloat+","+regexFloat+"\\)").matcher(divideString[0]);
//
//    while (m.find()) {
//        double x = Double.parseDouble(m.group(1));
//        double y = Double.parseDouble(m.group(2));
//        newMap.sleepBots.add(new Robot(x, y));
//    }
//
//    if (divideString.length < 2) continue;
//    String[] wallString = divideString[1].split(";");
//
//    for (int j = 0; j < wallString.length; j++) {
//        Wall newWall = new Wall();
//
//        m = Pattern.compile("\\("+regexFloat+","+regexFloat+"\\)").matcher(wallString[j]);
//
//        while (m.find()) {
//            double x = Double.parseDouble(m.group(1));
//            double y = Double.parseDouble(m.group(2));
//            newWall.vertices.add(new Point(x, y));
//        }
//        newMap.walls.add(newWall);
//    }
//    newMap.awakeBots.add(newMap.sleepBots.remove(0));
//    maps.add(newMap);
//}
//
//class Point {
//    public double x;
//    public double y;
//
//    public Point(double x, double y) {
//        this.x = x;
//        this.y = y;
//    }
//}
//
//class Robot extends Point {
//    public Robot(double x, double y) {
//        super(x, y);
//    }
//}
//
//class Wall {
//    public ArrayList<Point> vertices = new ArrayList<>();
//}
//
//class Map {
//    public ArrayList<Robot> awakeBots = new ArrayList<>();
//    public ArrayList<Robot> sleepBots = new ArrayList<>();
//    public ArrayList<Wall> walls = new ArrayList<>();
//}