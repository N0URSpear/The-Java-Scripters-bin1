package typingNinja;

import java.util.List;

public interface INinjaContactDAO {
    public void addNinjaUser(NinjaUser ninjaUser);

    public void updateNinjaUser(NinjaUser ninjaUser);

    public void deleteNinjaUser(NinjaUser ninjaUser);

    public NinjaUser getNinjaUser(String userName);

    public List<NinjaUser> getAllNinjas();
}
