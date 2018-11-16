package cn.jackwhliu.rvadapter.lib;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 万能的{@link RecyclerView}适配器。
 *
 * @author lwh
 * @param <BEAN> 适配的数据类型。
 */
public abstract class BaseRVAdapter<BEAN> extends RecyclerView.Adapter<BaseRVAdapter.ViewHolder> {

    /**
     * 用来加载条目的布局。
     */
    private final LayoutInflater mInflater;

    /**
     * 数据。
     */
    private volatile ArrayList<BEAN> mDatas;

    /**
     * 上下文。
     */
    private Context mContext;

    /**
     * 默认的替换条目的策略。
     */
    private ReplacePolicy mDefaultPolicy;

    /**
     * 条目点击事件。
     */
    private OnItemClickListener mOnItemClickListener;

    /**
     * 条目长按事件。
     */
    private OnItemLongClickListener mOnItemLongClickListener;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ItemViewIds {
        int[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ItemId {
        int value();
    }

    public BaseRVAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mDatas = new ArrayList<>();
        applyDefaultReplacePolicy();
    }

    public BaseRVAdapter(Context context, ArrayList<BEAN> datas) {
        this(context);
        this.mDatas = datas;
        applyDefaultReplacePolicy();
    }

    public BaseRVAdapter(Context context, BEAN[] datas) {
        this(context);
        bindDatas(Arrays.asList(datas));
        applyDefaultReplacePolicy();
    }

    public <ORIGIN> BaseRVAdapter(Context context, ORIGIN datas, DataConverter<ORIGIN, BEAN> converter) {
        this(context);
        bindDatas(converter.convertDatas(datas));
        applyDefaultReplacePolicy();
    }

    public interface DataConverter<T, BEAN> {
        ArrayList<BEAN> convertDatas(T datas);
    }

    public interface OnItemClickListener {
        void onItemClick(ViewGroup parent, int pos);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(ViewGroup parent, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        this.mOnItemLongClickListener = l;
    }

    /**
     * 绑定数据到适配器。
     *
     * @param datas 要绑定的数据。
     */
    private void bindDatas(List<BEAN> datas) {
        if (mDatas.size() > 0) {
            mDatas.clear();
        }
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    /**
     * 应用默认的替换策略。
     */
    private void applyDefaultReplacePolicy() {
        mDefaultPolicy = new DefaultReplacePolicy();
    }

    /**
     * 获取上下文。
     *
     * @return 上下文。
     */
    public Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int id = getItemId();
        int[] viewIds = getItemViewIds();
        Class<? extends BaseRVAdapter> adapterClass = getClass();
        ItemId itemId = adapterClass.getAnnotation(ItemId.class);
        if (itemId != null) {
            id = itemId.value();
        }
        ItemViewIds itemViewIds = adapterClass.getAnnotation(ItemViewIds.class);
        if (itemViewIds != null) {
            viewIds = itemViewIds.value();
        }
        View view = mInflater.inflate(id, parent, false);
        return new ViewHolder(view, viewIds);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BEAN data = getData(position);
        onBindViewHolder(holder, position, data);
        bindListeners(holder);
    }

    /**
     * 在此处理数据的加载等。
     *
     * @param holder View缓存对象。
     * @param position 条目在列表中的位置，从0开始。
     * @param data 给条目加载数据的模型对象。
     */
    public abstract void onBindViewHolder(ViewHolder holder, int position, BEAN data);

    /**
     * 绑定条目的点击事件和长按事件的监听。
     *
     * @param holder 缓存View的对象。
     */
    private void bindListeners(final ViewHolder holder) {
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick((ViewGroup) holder.itemView,
                            holder.getAdapterPosition());
                }
            });
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return mOnItemLongClickListener.onItemLongClick((ViewGroup) holder.itemView,
                            holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mDatas != null) {
            return mDatas.size();
        }
        return -1;
    }

    /**
     * 条目控件的id。
     *
     * @return 例如R.id.btn_01,R.id.btn_02,btn_03
     */
    protected abstract int[] getItemViewIds();

    /**
     * 条目布局文件的id。
     *
     * @return 例如item_example.xml
     */
    protected abstract int getItemId();

    /**
     * 主要用作控件的缓存优化。
     *
     * @param <VIEW> 要缓存的控件。
     */
    public class ViewHolder<VIEW extends View> extends RecyclerView.ViewHolder {

        /**
         * 缓存控件的稀疏数组。
         */
        private SparseArray<VIEW> mViewCache;

        public ViewHolder(View itemView, int[] itemViewIds) {
            super(itemView);
            mViewCache = new SparseArray<>();
            if (itemViewIds != null && itemViewIds.length > 0) {
                for (int id : itemViewIds) {
                    mViewCache.put(id, findViewById(id));
                }
            }
        }

        /**
         * 如果没有缓存，则创建一个以传入id为key的缓存控件；如果有缓存，则获取key等于传入id的缓存控件。
         *
         * @param id 用作缓存key的id。
         * @return 缓存的控件。
         */
        public VIEW findViewById(int id) {
            View view = mViewCache.get(id);
            if (view == null){
                view = itemView.findViewById(id);
                mViewCache.put(id, (VIEW) view);
            }
            return (VIEW) view;
        }

        public void setText(int textViewId, String text) {
            TextView textView = (TextView) findViewById(textViewId);
            textView.setText(text);
        }

        public void setImageResource(int imageViewId, int resId) {
            ImageView imageView = (ImageView) findViewById(imageViewId);
            imageView.setImageResource(resId);
        }
    }

    public void addItem(BEAN data) {
        mDatas.add(data);
        int position = mDatas.size() - 1;
        notifyItemInserted(position);
    }

    public void addItem(BEAN data, int index) {
        mDatas.add(index, data);
        notifyItemChanged(index);
    }

    public void addItems(List<BEAN> datas) {
        int lastSize = getItemCount();
        int newSize = datas.size();
        mDatas.addAll(datas);
        notifyItemRangeInserted(lastSize, newSize);
    }

    public void setItem(int position, BEAN data) {
        mDatas.set(position, data);
        notifyItemChanged(position);
    }

    public void setItems(int start, ArrayList<BEAN> datas) {
        setItems(start, datas, mDefaultPolicy);
    }

    public void setItems(ArrayList<BEAN> datas) {
        setItems(0, datas);
    }

    public void setItems(int start, ArrayList<BEAN> datas, ReplacePolicy policy) {
        if (datas.size()+start == mDatas.size()) {
            for (int i=start;i<getItemCount();i++) {
                mDatas.set(i, datas.get(start+i));
            }
        }
        if (datas.size() + start > mDatas.size()) {
            policy.replaceIfOutOfRange(this, mDatas, start, datas);
        }
        if (datas.size() + start < mDatas.size()) {
            policy.replaceIfNotUpToCapacity(this, mDatas, start, datas);
        }
    }

    /**
     * 用于确定条目的替换策略。
     *
     * @param <BEAN> 数据模型对象。
     */
    public interface ReplacePolicy<BEAN> {

        /**
         * 在给定条目数量超出已有条目的处理方式。
         *
         * @param adapter RV的适配器。
         * @param dstDatas 目标数据。
         * @param start 要替换的开始索引。
         * @param srcDatas 原始数据。
         */
        void replaceIfOutOfRange(RecyclerView.Adapter adapter, ArrayList<BEAN> dstDatas, int start,
                                 ArrayList<BEAN> srcDatas);

        /**
         * 在给定条目数量不足以覆盖所有已有条目的处理方式。
         *
         * @param adapter
         * @param dstDatas
         * @param start
         * @param srcDatas
         */
        void replaceIfNotUpToCapacity(RecyclerView.Adapter adapter, ArrayList<BEAN> dstDatas,
                                      int start, ArrayList<BEAN> srcDatas);
    }

    /**
     * 用于自定义数据替换的策略。
     *
     * @param policy 替换数据的策略。
     */
    public void setReplacePolicy(ReplacePolicy policy) {
        this.mDefaultPolicy = policy;
    }

    /**
     * 默认的替换策略，超出将忽略超出的条目，不足将只覆盖数据，不会影响到超出范围的。
     */
    private class DefaultReplacePolicy implements ReplacePolicy<BEAN> {

        @Override
        public void replaceIfOutOfRange(RecyclerView.Adapter adapter, ArrayList<BEAN> dstDatas,
                                        int start, ArrayList<BEAN> srcDatas) {
            int leftSize = dstDatas.size() - start;
            for (int i=start;i<leftSize;i++) {
                dstDatas.set(i, srcDatas.get(i));
            }
            adapter.notifyItemRangeChanged(start, leftSize);
        }

        @Override
        public void replaceIfNotUpToCapacity(RecyclerView.Adapter adapter,
                                             ArrayList<BEAN> dstDatas, int start, ArrayList<BEAN> srcDatas) {
            int srcSize = srcDatas.size();
            for (int i=start;i<start+srcSize;i++) {
                dstDatas.set(i, srcDatas.get(i));
            }
            adapter.notifyItemRangeChanged(start, srcSize);
        }
    }

    /**
     * 移除数据。
     *
     * @param position 要移除数据的下标。
     */
    public void removeItem(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 移除数据。
     *
     * @throws ArrayIndexOutOfBoundsException
     * @param start 从哪条记录开始？
     * @param count 移除数据的条数。
     */
    public void removeItem(int start, int count) {
        int end = start + count;
        for (int i=start;i<end;i++) {
            mDatas.remove(i);
        }
        notifyItemRangeRemoved(start, count);
    }

    /**
     * 清空所有的条目。
     */
    public void clear() {
        int dataSize = mDatas.size();
        mDatas.clear();
        notifyItemRangeRemoved(0, dataSize);
    }

    /**
     * 获取所有的Bean数据。
     *
     * @return Bean数据集合。
     */
    public List<BEAN> getDatas() {
        return mDatas;
    }

    /**
     * 获取指定位置的Bean数据。
     *
     * @param position 要获取的Bean数据的位置。
     * @return Bean数据。
     */
    public BEAN getData(int position) {
        return mDatas.get(position);
    }

    /**
     * 置顶指定条目。
     *
     * @param position 需要置顶的条目的位置。
     */
    public void stickItem(int position) {
        BEAN data = mDatas.get(position);
        removeItem(position);
        addItem(data);
    }

    /**
     * 将条目的顺序倒过来。
     */
    public void reverseItems() {
        Collections.reverse(mDatas);
        notifyDataSetChanged();
    }
}
