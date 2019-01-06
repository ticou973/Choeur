package dedicace.com.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dedicace.com.data.ChoraleRepository;

public class MainActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ChoraleRepository mRepository;

    public MainActivityViewModelFactory(ChoraleRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MainActivityViewModel(mRepository);
    }


}
