package fr.eurecom.mobserv.arianna.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * @author uccio
 *
 */
public class NavigationNode extends Model implements BaseColumns,Comparable<NavigationNode> {
	
	public static final String TABLE_NAME = "navigation_node";
    public static final String COLUMN_NAME_X = "x";
    public static final String COLUMN_NAME_Y = "y";
    public static final String COLUMN_NAME_MAP_LEVEL = "map_level";
    public static final String[] COLUMNS_NAME={Model.COLUMN_NAME_URI,COLUMN_NAME_X,COLUMN_NAME_Y,COLUMN_NAME_MAP_LEVEL};
	
    public static final String SQL_CREATE_TABLE = 
    		"CREATE TABLE " + NavigationNode.TABLE_NAME + " ("
    		+ NavigationNode._ID + Model.PRIMARY_KEY + Model.COMMA_SEP
    		+ Model.COLUMN_NAME_URI + Model.TEXT_TYPE+UNIQUE + Model.COMMA_SEP
    		+ NavigationNode.COLUMN_NAME_X + Model.COORDINATE_TYPE + Model.COMMA_SEP
    		+ NavigationNode.COLUMN_NAME_Y + Model.COORDINATE_TYPE+ Model.COMMA_SEP
    		+ NavigationNode.COLUMN_NAME_MAP_LEVEL + Model.TEXT_TYPE + COMMA_SEP
    		+ "FOREIGN KEY("+NavigationNode.COLUMN_NAME_MAP_LEVEL+") REFERENCES "+ MapLevel.TABLE_NAME +"("+Model.COLUMN_NAME_URI+")"+ 
    		")";
    
    
    private int x;
    private int y;
    private MapLevel mapLevel;
    private Map<String,NavigationLink> outLinks;
    private Map<String,NavigationLink> inLinks;
    private Map<String,PointOfInterest> pois;
    
    /** for algorithm shortest path**/
    public double minDistance = Double.POSITIVE_INFINITY;
    private NavigationLink linkToPrevious = null;
    public List<NavigationLink> adjacencies = new ArrayList<NavigationLink>();
    private NavigationNode previous;

    
	
	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * 	
	 * @param context
	 * @param URI
	 * @param x horizontal coordinate of the navigation node on the map level image (origin is on the upper-left corner) 
	 * @param y vertical coordinate of the navigation node on the map level image (origin is on the upper-left corner) 
	 * @param mapLevel URI of the map level where the navigation node is situated  
	 */
	public NavigationNode(Context context, String URI, int x, int y, MapLevel mapLevel) {
		super(context, URI);
		this.x = x;
		this.y = y;
		this.mapLevel = mapLevel;
	}
	
	/**
	 * @return the mapLevel
	 */
	public MapLevel getMapLevel() {
		return mapLevel;
	}
	/**
	 * @param mapLevel the mapLevel to set
	 */
	public void setMapLevel(MapLevel mapLevel) {
		this.mapLevel = mapLevel;
	}
	/**
	 * @param context
	 * @param cursor result from query to map DB entry and JAVA object
	 */
	public NavigationNode(Context context, Cursor cursor) {
		super(context,cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_URI)));
		this.x = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_X));
		this.y=cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_Y));
		this.mapLevel = (MapLevel) Model.getByURI(MapLevel.class, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_MAP_LEVEL)),this.getContext());;
	}
	
	@Override
	public boolean save() {
		// Gets the data repository in write mode
		SQLiteDatabase db = DbHelper.getInstance(this.getContext()).getWritableDatabase();
	
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(Model.COLUMN_NAME_URI,this.getUri());
		values.put(NavigationNode.COLUMN_NAME_X, this.getX());
		values.put(NavigationNode.COLUMN_NAME_Y, this.getY());
		values.put(NavigationNode.COLUMN_NAME_MAP_LEVEL, this.getMapLevel().getUri());
		
		// Insert the new row, returning the primary key value of the new row
		long newRowId;
		newRowId = db.insert(
		         NavigationNode.TABLE_NAME,
		         null,
		         values);
		return newRowId>=0;
	}
	
	/**
	 * @return the outLinks
	 */
	public Map<String, NavigationLink> getOutLinks() {
		if(this.outLinks == null || this.outLinks.isEmpty()){
			Map<String,NavigationLink>links=(Map)Model.getByParam(NavigationLink.class, NavigationLink.COLUMN_NAME_FROM_NODE, this.getUri(), this.getContext());
			this.outLinks=links;
		}
		return outLinks;
	}
	/**
	 * @param outLinks the outLinks to set
	 */
	public void setOutLinks(Map<String, NavigationLink> outLinks) {
		this.outLinks = outLinks;
	}
	/**
	 * @return the inLinks
	 */
	public Map<String, NavigationLink> getInLinks() {
		if(this.inLinks == null || this.inLinks.isEmpty()){
			Map<String,NavigationLink>links=(Map)Model.getByParam(NavigationLink.class, NavigationLink.COLUMN_NAME_TO_NODE, this.getUri(), this.getContext());
			this.inLinks=links;
		}
		return inLinks;
	}
	/**
	 * @param inLinks the inLinks to set
	 */
	public void setInLinks(Map<String, NavigationLink> inLinks) {
		this.inLinks = inLinks;
	}
	@Override
	protected String getTableName() {
		return NavigationNode.TABLE_NAME;
	}
	/**
	 * @return the pois
	 */
	public Map<String,PointOfInterest> getPois() {
		if(this.pois == null || this.pois.isEmpty()){
			Map<String,PointOfInterest>pois=(Map)Model.getByParam(PointOfInterest.class, PointOfInterest.COLUMN_NAME_NAV_NODE, this.getUri(), this.getContext());
			this.pois=pois;
		}
		return pois;
	}
	/**
	 * @param pois the pois to set
	 */
	public void setPois(Map<String,PointOfInterest> pois) {
		this.pois = pois;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mapLevel == null) ? 0 : mapLevel.hashCode());
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NavigationNode other = (NavigationNode) obj;
		if (mapLevel == null) {
			if (other.mapLevel != null)
				return false;
		} else if (!mapLevel.equals(other.mapLevel))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
	@Override
	public int compareTo(NavigationNode another) {
		return Double.compare(minDistance, another.minDistance);
	}
	/**
	 * @return the linkToPrevious
	 */
	public NavigationLink getLinkToPrevious() {
		NavigationLink linkprev = linkToPrevious;
//		linkToPrevious = null;
		return linkprev;
	}
	/**
	 * @param linkToPrevious the linkToPrevious to set
	 */
	public void setLinkToPrevious(NavigationLink linkToPrevious) {
		this.linkToPrevious = linkToPrevious;
	}
	/**
	 * @return the previous
	 */
	public NavigationNode getPrevious() {
		NavigationNode prev = previous;
//		previous = null;
		return prev;
	}
	/**
	 * @param previous the previous to set
	 */
	public void setPrevious(NavigationNode previous) {
		this.previous = previous;
	}
	
	

    

}
