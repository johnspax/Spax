package movie.software.com.spax.menu;

public class MenuURL {
    public String Path;
    public String title;
    public String body;
    public String phone;
    public MenuURL(){
        super();
    }

    public MenuURL(String Path, String title, String body, String phone) {
        super();
        this.Path = Path;
        this.title = title;
        this.body = body;
        this.phone = phone;
    }
}
