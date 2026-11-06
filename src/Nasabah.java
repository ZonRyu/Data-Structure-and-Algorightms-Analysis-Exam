/**
 * Model data seorang nasabah yang mengantre di bank.
 */
public class Nasabah {

    /** Jenis layanan nasabah; VIP diprioritaskan daripada REGULER. */
    public enum Tipe { VIP, REGULER }

    private final int id;
    private final String nama;
    private final Tipe tipe;
    private int waktuTunggu; // jumlah layanan (dequeue) yang sudah terjadi selama nasabah ini mengantre

    public Nasabah(int id, String nama, Tipe tipe) {
        this.id = id;
        this.nama = nama;
        this.tipe = tipe;
        this.waktuTunggu = 0;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public Tipe getTipe() {
        return tipe;
    }

    public int getWaktuTunggu() {
        return waktuTunggu;
    }

    /** Menambah waktu tunggu satu satuan; dipanggil antrean setiap kali ada nasabah lain dilayani. */
    public void tambahWaktuTunggu() {
        waktuTunggu++;
    }

    @Override
    public String toString() {
        return "#" + id + " " + nama + " (" + tipe + ", tunggu=" + waktuTunggu + ")";
    }
}
