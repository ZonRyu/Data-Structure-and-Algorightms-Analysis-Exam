/**
 * Simulasi yang membuktikan nasabah reguler tidak mengalami starvation.
 * Setiap skenario akan melempar AssertionError jika jaminan tersebut dilanggar.
 */
public class Main {

    private static final int RASIO_VIP = 3;   // maksimal 3 VIP beruntun saat ada reguler menunggu
    private static final int BATAS_AGING = 5; // reguler yang menunggu >= 5 layanan boleh menyalip VIP

    public static void main(String[] args) {
        skenarioKedatanganBeruntun();
        skenarioVipTerusDatang();
    }

    /** Pola kedatangan VIP, VIP, Reguler berulang lalu semua dilayani; rasio dan aging sama-sama aktif. */
    private static void skenarioKedatanganBeruntun() {
        System.out.println("=== Skenario 1: Kedatangan beruntun ===");
        AntiStarvationQueue antrean = new AntiStarvationQueue(12, RASIO_VIP, BATAS_AGING);

        int id = 1;
        for (int grup = 1; grup <= 4; grup++) {
            antrean.enqueueVIP(buatVip(id++));
            antrean.enqueueVIP(buatVip(id++));
            antrean.enqueueReguler(buatReguler(id++));
        }
        antrean.displayQueue();

        int vipBeruntun = 0;
        while (!antrean.isEmpty()) {
            vipBeruntun = layaniDanPeriksa(antrean, vipBeruntun);
        }
        System.out.println("Hasil: semua nasabah terlayani.\n");
    }

    /** Kasus terburuk: setiap kali satu nasabah dilayani, satu VIP baru datang. */
    private static void skenarioVipTerusDatang() {
        System.out.println("=== Skenario 2: VIP terus datang ===");
        AntiStarvationQueue antrean = new AntiStarvationQueue(6, RASIO_VIP, BATAS_AGING);

        int id = 1;
        antrean.enqueueReguler(buatReguler(id++));
        antrean.enqueueReguler(buatReguler(id++));
        antrean.enqueueVIP(buatVip(id++));
        antrean.enqueueVIP(buatVip(id++));
        antrean.displayQueue();

        int vipBeruntun = 0;
        for (int layanan = 1; layanan <= 10; layanan++) {
            antrean.enqueueVIP(buatVip(id++));
            vipBeruntun = layaniDanPeriksa(antrean, vipBeruntun);
        }
        antrean.displayQueue();

        if (antrean.getJumlahReguler() > 0) {
            throw new AssertionError("Reguler masih terabaikan meski VIP terus datang");
        }
        System.out.println("Hasil: semua reguler terlayani walau VIP tidak pernah berhenti datang.");
    }

    /**
     * Melayani satu nasabah dan memastikan VIP beruntun tidak melebihi rasio selama ada reguler menunggu.
     *
     * @return jumlah VIP beruntun terbaru selama ada reguler yang menunggu
     */
    private static int layaniDanPeriksa(AntiStarvationQueue antrean, int vipBeruntun) {
        Nasabah dilayani = antrean.dequeue();
        System.out.println("Dilayani: " + dilayani);

        boolean vipMendahuluiReguler = dilayani.getTipe() == Nasabah.Tipe.VIP && antrean.getJumlahReguler() > 0;
        int vipBeruntunBaru = vipMendahuluiReguler ? vipBeruntun + 1 : 0;

        if (vipBeruntunBaru > RASIO_VIP) {
            throw new AssertionError("Starvation: " + vipBeruntunBaru + " VIP beruntun mendahului reguler");
        }
        return vipBeruntunBaru;
    }

    private static Nasabah buatVip(int id) {
        return new Nasabah(id, "VIP-" + id, Nasabah.Tipe.VIP);
    }

    private static Nasabah buatReguler(int id) {
        return new Nasabah(id, "Reguler-" + id, Nasabah.Tipe.REGULER);
    }
}
