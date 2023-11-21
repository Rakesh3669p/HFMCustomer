package com.hfm.customer.ui.fragments.wishlist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R

import com.hfm.customer.databinding.ItemShopsBinding
import com.hfm.customer.ui.fragments.wishlist.model.Favourite
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject
import kotlin.math.roundToInt

class WishlistShopAdapter @Inject constructor(): RecyclerView.Adapter<WishlistShopAdapter.ViewHolder>() {
    private lateinit var  context: Context

    inner class  ViewHolder(private val itemBannerBinding: ItemShopsBinding):RecyclerView.ViewHolder(itemBannerBinding.root){
        fun bind(data: Favourite) {

            with(itemBannerBinding){
                shopImage.loadImage(replaceBaseUrl(data.banner))
                shopName.text = data.store_name
                ratingBar.rating = data.store_rating.toFloat()
                ratingType.text ="${data.postive_review.roundToInt()} % Positive"
                ratings.text ="${data.store_rating} ratings"
                products.text =data.no_of_products.toString()
                country.text = data.country
                state.text = data.state

                unFollow.setOnClickListener {
                    onUnFollowClick?.let {
                        it(data.seller_id)
                    }
                }
                chat.setOnClickListener {
                    onChatClick?.let {
                        it(data)
                    }
                }

                root.setOnClickListener {
                    onItemClick?.let {
                        it(data.seller_id)
                    }
                }
            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<Favourite>(){
        override fun areItemsTheSame(oldItem: Favourite, newItem: Favourite): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Favourite, newItem: Favourite): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(ItemShopsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])

    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onUnFollowClick: ((id: Int) -> Unit)? = null

    fun setOnUnFollowClickListener(listener: (id: Int) -> Unit) {
        onUnFollowClick = listener
    }

    private var onChatClick: ((id: Favourite) -> Unit)? = null

    fun setOnChatClickListener(listener: (id: Favourite) -> Unit) {
        onChatClick = listener
    }

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }
}