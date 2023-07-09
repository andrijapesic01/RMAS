import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import elfak.mosis.iride.Car
import elfak.mosis.iride.R
import elfak.mosis.iride.databinding.ItemCarBinding

class CarListAdapter(private var cars: List<Car>) : RecyclerView.Adapter<CarListAdapter.CarViewHolder>() {

    private var onCarClickListener: OnCarClickListener? = null

    interface OnCarClickListener {
        fun onCarClick(car: Car)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCarBinding.inflate(inflater, parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.bind(car)
    }

    override fun getItemCount(): Int {
        return cars.size
    }

    fun setCars(cars: List<Car>) {
        this.cars = cars
        notifyDataSetChanged()
    }

    fun setOnCarClickListener(listener: OnCarClickListener) {
        onCarClickListener = listener
    }

    inner class CarViewHolder(private val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(car: Car) {
            binding.textViewCarMakeModel.text = "${car.brand} ${car.model}"

            Glide.with(itemView)
                .load(car.carImage)
                .placeholder(R.drawable.car_placeholder)
                .into(binding.imageViewCar)

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val car = cars[position]
                    onCarClickListener?.onCarClick(car)
                }
            }
        }
    }

}
