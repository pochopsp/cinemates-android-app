package it.unina.cinemates.ui.common.interfaces;

import android.view.View;

public interface OnLongClickListenerGenerator<T> {
    View.OnLongClickListener generate(T onClickArgument);
}
