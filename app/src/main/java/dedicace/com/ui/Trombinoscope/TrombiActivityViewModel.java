package dedicace.com.ui.Trombinoscope;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import dedicace.com.data.database.Choriste;

public class TrombiActivityViewModel extends ViewModel {
    private TrombiRepository mRepository;
    private LiveData<List<Choriste>> choristes;
    private String typeChoriste;


    public TrombiActivityViewModel(TrombiRepository mRepository) {
        this.mRepository = mRepository;
        choristes = mRepository.getChoristes();
    }

    public LiveData<List<Choriste>> getChoristes() {
        return choristes;
    }


    public Thread getCurrentThread() {
        return mRepository.getCurrentThread();
    }

    public String getTypeChoriste() {
        typeChoriste=mRepository.getTypeChoriste();

        return typeChoriste;
    }

    public boolean getDeleted() {
        return mRepository.getDeleted();
    }
}
