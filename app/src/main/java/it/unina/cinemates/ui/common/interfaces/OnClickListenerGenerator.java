package it.unina.cinemates.ui.common.interfaces;

import android.view.View;

public interface OnClickListenerGenerator<T> {
    View.OnClickListener generate(T onClickArgument);
}
