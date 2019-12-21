package com.uet.teamduan.musicplayer.models.data


import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable


open class Song(
        var id: Long =0,
        var albumId: Long=0,
        var title: String = "",
        var artist:String?="",
        var path:String = "",
        var thumb: Bitmap? = null,
        var duration: Long = 0,
        var date: Long = 0,
        var size:Long = 0
) : Parcelable {

    var liked:Boolean = false
    var selected:Boolean = false
    var isPlaying:Boolean = false

    var firebaseStorageKey:String = ""
  constructor(parcel: Parcel)
          :this(
          parcel.readLong(),
          parcel.readLong(),
          parcel.readString()!!,
          parcel.readString()!!,
          parcel.readString()!!,
          null,// parcel.readParcelable(Bitmap::class.java.classLoader)
          parcel.readLong(),
          parcel.readLong(),
          parcel.readLong())
  {}


  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel?, flags: Int) {
    dest!!.writeLong(id)
    dest.writeLong(albumId)
    dest.writeString(title)
    dest.writeString(artist)
    dest.writeString(path)

    dest.writeLong(duration)
    dest.writeLong(date)
    dest.writeLong(size)
  }



  companion object CREATOR:Parcelable.Creator<Song>
      {
          override fun createFromParcel(source: Parcel?): Song {
              return Song(source!!)
          }

          override fun newArray(size: Int): Array<Song?> {
              return arrayOfNulls(size)
          }


      }

  fun copyObject():Song{
    return Song(this.id,this.albumId, this.title, this.artist, this.path, this.thumb, this.duration, this.date, this.size)
  }
}
