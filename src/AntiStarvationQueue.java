/**
 * Antrean prioritas anti-starvation di atas satu array sirkular primitif.
 *
 * Susunan logis antrean selalu dijaga seperti ini (dihitung dari head):
 *
 *     posisi:  0 ... vipCount-1 | vipCount ... size-1
 *     isi:     VIP (FIFO)       | Reguler (FIFO)
 *
 * Akibatnya, VIP terdepan selalu di posisi 0 dan reguler tertua selalu di
 * posisi vipCount, sehingga keduanya ditemukan tanpa perlu mencari.
 *
 * Aturan dequeue:
 * 1. VIP didahulukan.
 * 2. Rasio: setelah {@code rasioVip} VIP beruntun, reguler wajib dilayani.
 * 3. Aging: reguler yang menunggu >= {@code batasAging} boleh menyalip VIP,
 *    asalkan sudah ada minimal satu VIP dilayani sejak reguler terakhir.
 *    Syarat ini mencegah kebalikannya, yaitu VIP yang kelaparan ketika
 *    banyak reguler sudah berumur.
 * 4. Jika hanya ada satu tipe di antrean, tipe tersebut langsung dilayani.
 *
 * Kompleksitas waktu (n = size):
 * - enqueueReguler: O(1)
 * - enqueueVIP    : O(n), perlu menggeser reguler
 * - dequeue       : O(n), perlu memperbarui waktu tunggu semua nasabah
 * - displayQueue  : O(n)
 */
public class AntiStarvationQueue {

    private final Nasabah[] data;
    private final int capacity;
    private final int rasioVip;   // batas VIP beruntun sebelum reguler wajib dilayani
    private final int batasAging; // waktu tunggu minimum agar reguler boleh menyalip VIP

    private int head;        // indeks fisik elemen terdepan
    private int tail;        // indeks fisik slot kosong berikutnya
    private int size;        // jumlah seluruh nasabah di antrean
    private int vipCount;    // jumlah nasabah VIP; juga posisi logis reguler tertua
    private int vipBeruntun; // jumlah VIP yang dilayani sejak reguler terakhir dilayani

    public AntiStarvationQueue(int capacity, int rasioVip, int batasAging) {
        if (capacity <= 0 || rasioVip <= 0 || batasAging <= 0) {
            throw new IllegalArgumentException("capacity, rasioVip, dan batasAging harus lebih dari 0");
        }
        this.data = new Nasabah[capacity];
        this.capacity = capacity;
        this.rasioVip = rasioVip;
        this.batasAging = batasAging;
    }

    /** Menyisipkan nasabah VIP tepat setelah VIP terakhir, di depan semua reguler. */
    public void enqueueVIP(Nasabah n) {
        pastikanBisaMasuk(n, Nasabah.Tipe.VIP);

        // Geser semua reguler satu langkah ke belakang untuk membuka slot di posisi vipCount.
        for (int posisi = size; posisi > vipCount; posisi--) {
            data[indeksFisik(posisi)] = data[indeksFisik(posisi - 1)];
        }
        data[indeksFisik(vipCount)] = n;

        vipCount++;
        size++;
        tail = indeksBerikutnya(tail);
    }

    /** Menambahkan nasabah reguler di ujung belakang antrean. */
    public void enqueueReguler(Nasabah n) {
        pastikanBisaMasuk(n, Nasabah.Tipe.REGULER);

        data[tail] = n;
        size++;
        tail = indeksBerikutnya(tail);
    }

    /**
     * Mengambil dan menghapus nasabah berikutnya sesuai aturan anti-starvation.
     *
     * @return nasabah yang dilayani
     * @throws IllegalStateException jika antrean kosong
     */
    public Nasabah dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Antrean kosong");
        }

        int posisiDilayani = giliranRegulerMenyalip() ? vipCount : 0;
        Nasabah dilayani = hapusPada(posisiDilayani);

        if (dilayani.getTipe() == Nasabah.Tipe.VIP) {
            vipCount--;
            vipBeruntun++;
        } else {
            vipBeruntun = 0;
        }

        tambahWaktuTungguSemua();
        return dilayani;
    }

    /** Menampilkan isi antrean sesuai susunan logisnya, dari posisi terdepan. */
    public void displayQueue() {
        System.out.println("Antrean [" + size + "/" + capacity + "] VIP=" + vipCount
                + " Reguler=" + getJumlahReguler());
        for (int posisi = 0; posisi < size; posisi++) {
            System.out.println("  " + (posisi + 1) + ". " + data[indeksFisik(posisi)]);
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int getJumlahReguler() {
        return size - vipCount;
    }

    /** Menentukan apakah reguler tertua harus dilayani lebih dulu daripada VIP terdepan. */
    private boolean giliranRegulerMenyalip() {
        if (vipCount == 0 || getJumlahReguler() == 0) {
            return false; // hanya ada satu tipe, jadi nasabah di posisi 0 yang dilayani
        }

        boolean rasioTerpenuhi = vipBeruntun >= rasioVip;
        boolean regulerTertuaBerumur = data[indeksFisik(vipCount)].getWaktuTunggu() >= batasAging;
        return rasioTerpenuhi || (regulerTertuaBerumur && vipBeruntun > 0);
    }

    /**
     * Menghapus nasabah pada posisi logis tertentu. Elemen di depannya digeser
     * satu langkah ke belakang lalu head dimajukan, sehingga penghapusan di
     * posisi 0 tidak memerlukan pergeseran sama sekali.
     */
    private Nasabah hapusPada(int posisi) {
        Nasabah dihapus = data[indeksFisik(posisi)];

        for (int p = posisi; p > 0; p--) {
            data[indeksFisik(p)] = data[indeksFisik(p - 1)];
        }

        data[head] = null; // melepas referensi lama agar bisa dibersihkan garbage collector
        head = indeksBerikutnya(head);
        size--;
        return dihapus;
    }

    private void tambahWaktuTungguSemua() {
        for (int posisi = 0; posisi < size; posisi++) {
            data[indeksFisik(posisi)].tambahWaktuTunggu();
        }
    }

    /** Validasi bersama enqueue: tipe yang salah akan merusak susunan VIP | Reguler. */
    private void pastikanBisaMasuk(Nasabah n, Nasabah.Tipe tipeSeharusnya) {
        if (n == null || n.getTipe() != tipeSeharusnya) {
            throw new IllegalArgumentException("Nasabah harus bertipe " + tipeSeharusnya);
        }
        if (size == capacity) {
            throw new IllegalStateException("Antrean penuh");
        }
    }

    /** Mengubah posisi logis (dihitung dari head) menjadi indeks fisik pada array sirkular. */
    private int indeksFisik(int posisi) {
        return (head + posisi) % capacity;
    }

    private int indeksBerikutnya(int indeks) {
        return (indeks + 1) % capacity;
    }
}
