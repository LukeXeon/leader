package kexie.android.common.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.lang.reflect.Method;

public class BindingRecyclerAdapter<T>
        extends BaseQuickAdapter<T,BaseViewHolder>
{

    private final String setterName;
    private Method setter;

    public BindingRecyclerAdapter(String variableName, @LayoutRes int layoutResId)
    {
        super(layoutResId);
        if (TextUtils.isEmpty(variableName))
        {
            throw new IllegalArgumentException();
        }
        this.setterName = "set" + String.valueOf(variableName.charAt(0)
                + variableName.substring(1));
    }

    @Override
    protected void convert(BaseViewHolder helper, T data)
    {
        try
        {
            ViewDataBinding binding = DataBindingUtil.bind(helper.itemView);
            assert binding != null;
            getSetter(binding,data).invoke(binding, data);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private Method getSetter(ViewDataBinding binding,T data)
    {
        if (setter == null)
        {
            for (Method method : binding.getClass().getMethods())
            {
                Class<?>[] parameters = method.getParameterTypes();
                if (method.getName().equals(setterName)
                        && parameters.length == 1
                        && parameters[0].isInstance(data))
                {
                    setter = method;
                    return setter;
                }
            }
            throw new RuntimeException();
        }
        else
        {
            return setter;
        }
    }
}
