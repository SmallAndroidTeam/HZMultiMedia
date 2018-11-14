package of.media.hz.info;

/**
 * 项目名称：EditSearch
 * 类描述：
 * 创建人：tonycheng
 * 创建时间：2016/4/25 17:30
 * 邮箱：tonycheng93@outlook.com
 * 修改人：
 * 修改时间：
 * 修改备注：
 */
public class User {
    private int avatarResId;
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private String name;

    public User(int avatarResId, String name) {
        this.avatarResId = avatarResId;
        this.name = name;
    }
    public User(String image, String name) {
        this.image = image;
        this.name = name;
    }
    public int getAvatarResId() {
        return avatarResId;
    }

    public void setAvatarResId(int avatarResId) {
        this.avatarResId = avatarResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
