package dedicace.com.ui.Trombinoscope;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.util.Log;


public class TrombiActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory{
    private final TrombiRepository mRepository;

    public TrombiActivityViewModelFactory(TrombiRepository repository) {
        this.mRepository = repository;
        Log.d("coucou", "TrombiViewModelFactory: constructor");
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new TrombiActivityViewModel(mRepository);
    }
}
