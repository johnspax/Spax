package movie.software.com.spax.menu;

/**
 * Created by John Muya on 08/03/2017.
 */

public class Menu {
    //public int icon;
    public String PosterPath;
    public String title;
    public String body;
    public String phone;
    public String OrderDate;
    public Menu(){
        super();
    }

    public Menu(String PosterPath, String title, String body, String phone, String OrderDate) {
        super();
        //this.icon = icon;
        this.PosterPath = PosterPath;
        this.title = title;
        this.body = body;
        this.phone = phone;
        this.OrderDate = OrderDate;
    }
}

