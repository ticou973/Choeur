package dedicace.com.ui.Trombinoscope;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import dedicace.com.data.database.Choriste;

public class TrombiActivityViewModel extends ViewModel {
    private TrombiRepository mRepository;
    private LiveData<List<Choriste>> choristes;


    public TrombiActivityViewModel(TrombiRepository mRepository) {
        this.mRepository = mRepository;
        choristes = mRepository.getChoristes();
    }

    public LiveData<List<Choriste>> getChoristes() {
        return choristes;
    }
}
