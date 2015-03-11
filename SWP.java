/*===============================================================*
 *  File: SWP.java                                               *
 *                                                               *
 *  This class implements the sliding window protocol            *
 *  Used by VMach class					         *
 *  Uses the following classes: SWE, Packet, PFrame, PEvent,     *
 *                                                               *
 *  Author: Professor SUN Chengzheng                             *
 *          School of Computer Engineering                       *
 *          Nanyang Technological University                     *
 *          Singapore 639798                                     *
 *===============================================================*/

import java.util.Timer;
import java.util.TimerTask;

public class SWP {

    /*========================================================================*
     the following are provided, do not change them!!
     *========================================================================*/
    //the following are protocol constants.
    public static final int MAX_SEQ = 7;
    public static final int NR_BUFS = (MAX_SEQ + 1) / 2;

    // the following are protocol variables
    private int oldest_frame = 0;
    private PEvent event = new PEvent();
    private Packet out_buf[] = new Packet[NR_BUFS];

    //the following are used for simulation purpose only
    private SWE swe = null;
    private String sid = null;

    //Constructor
    public SWP(SWE sw, String s) {
        swe = sw;
        sid = s;
    }

    //the following methods are all protocol related
    private void init() {
        for (int i = 0; i < NR_BUFS; i++) {
            out_buf[i] = new Packet();
        }
    }

    private void wait_for_event(PEvent e) {
        swe.wait_for_event(e); //may be blocked
        oldest_frame = e.seq;  //set timeout frame seq
    }

    private void enable_network_layer(int nr_of_bufs) {
        //network layer is permitted to send if credit is available
        swe.grant_credit(nr_of_bufs);
    }

    private void from_network_layer(Packet p) {
        swe.from_network_layer(p);
    }

    private void to_network_layer(Packet packet) {
        swe.to_network_layer(packet);
    }

    private void to_physical_layer(PFrame fm) {
        System.out.println("SWP: Sending frame: seq = " + fm.seq +
                " ack = " + fm.ack + " kind = " +
                PFrame.KIND[fm.kind] + " info = " + fm.info.data);
        System.out.flush();
        swe.to_physical_layer(fm);
    }

    private void from_physical_layer(PFrame fm) {
        PFrame fm1 = swe.from_physical_layer();
        fm.kind = fm1.kind;
        fm.seq = fm1.seq;
        fm.ack = fm1.ack;
        fm.info = fm1.info;
    }


/*===========================================================================*
     implement your Protocol Variables and Methods below:
 *==========================================================================*/

    static boolean no_nak = true;
    Packet in_buf[] = new Packet[NR_BUFS];            /* buffers for the inbound stream */

    public void protocol6() {
        int next_frame_to_send = 0, frame_expected = 0, ack_expected = 0;
        int too_far = NR_BUFS;
        PFrame r = new PFrame();
        boolean arrived[] = new boolean[NR_BUFS];

        enable_network_layer(NR_BUFS);
        for (int i = 0; i < NR_BUFS; i++)
            arrived[i] = false;

        init();
        while (true) {
            wait_for_event(event);
            switch (event.type) {
                case (PEvent.NETWORK_LAYER_READY):
                    from_network_layer(out_buf[next_frame_to_send % NR_BUFS]);
                    send_frame(PFrame.DATA, next_frame_to_send, frame_expected, out_buf);
                    next_frame_to_send = inc(next_frame_to_send);
                    break;
                case (PEvent.FRAME_ARRIVAL):
                    from_physical_layer(r);
                    if (r.kind == PFrame.DATA) {
                        if ((r.seq != frame_expected) && no_nak)
                            send_frame(PFrame.NAK, 0, frame_expected, out_buf);
                        else
                            start_ack_timer();

                        if (between(frame_expected, r.seq, too_far) && (arrived[r.seq % NR_BUFS] == false)) {
                            arrived[r.seq % NR_BUFS] = true;     /* mark buffer as full*/
                            in_buf[r.seq % NR_BUFS] = r.info;    /*insert data into buffer*/
                            while (arrived[frame_expected % NR_BUFS]) {
                            /* Pass frames and advance window.*/
                                to_network_layer(in_buf[frame_expected % NR_BUFS]);
                                no_nak = true;
                                arrived[frame_expected % NR_BUFS] = false;
                                //debug line
                                //System.out.println("********expected:"+frame_expected+"seq:"+r.seq+"too_far:"+(too_far));

                                frame_expected = inc(frame_expected);    /* advance lower edge of receiver's window */
                                too_far = inc(too_far);                  /* advance upper edge of receiver's window */
                                //System.out.println("============start ack timer!!!"); //debug line
                                start_ack_timer();                     /*to see if a separate ack is needed */
                            }
                        }
                    }
                    //selective repeat
                    //resend the frame lost (last correct frame sequence+1)
                    if ((r.kind == PFrame.NAK) && between(ack_expected, (r.ack + 1) % (MAX_SEQ + 1), next_frame_to_send))
                        send_frame(PFrame.DATA, (r.ack + 1) % (MAX_SEQ + 1), frame_expected, out_buf);


                    //whenever frame is received
                    while (between(ack_expected, r.ack, next_frame_to_send)) {
                        // nbuffered = nbuffered-1; // delete this line as the window size is fixed
                        stop_timer(ack_expected); /*frame arrived intact*/
                        ack_expected = inc(ack_expected); /* advance lower edge of sender's window */
                        enable_network_layer(1);  // allow one more frame to fulfill the buffer
                    }
                    break;
                case (PEvent.CKSUM_ERR):
                    if (no_nak)
                        send_frame(PFrame.NAK, 0, frame_expected, out_buf);
                    break;
                case (PEvent.TIMEOUT):
                    if (between(ack_expected, oldest_frame, next_frame_to_send))
                        send_frame(PFrame.DATA, oldest_frame, frame_expected, out_buf);
                    break;
                case (PEvent.ACK_TIMEOUT):
                    send_frame(PFrame.ACK, 0, frame_expected, out_buf);
                    break;
                default:
                    System.out.println("SWP: undefined event type = " + event.type);
                    System.out.flush();
            }
        }
    }

    public int inc(int frame_nr) {
        frame_nr++;
        if (frame_nr > MAX_SEQ)
            frame_nr = 0;
        return frame_nr;
    }

    void send_frame(int frame_kind, int frame_nr, int frame_expected, Packet buffer[]) {
      /*Construct and send a data frame.*/
        PFrame s = new PFrame();

        s.kind = frame_kind;
        if (frame_kind == PFrame.DATA)
            s.info = buffer[frame_nr % NR_BUFS];
        s.seq = frame_nr;
        s.ack = (frame_expected + MAX_SEQ) % (MAX_SEQ + 1);
        if (frame_kind == PFrame.NAK)
            no_nak = false;
        to_physical_layer(s);
        if (frame_kind == PFrame.DATA)
            start_timer(frame_nr);
        stop_ack_timer();
    }

    static boolean between(int a, int b, int c) {
        return ((a <= b) && (b < c)) || ((c < a) && (a <= b)) || ((b < c) && (c < a));
    }


 /* Note: when start_timer() and stop_timer() are called, 
    the "seq" parameter must be the sequence number, rather 
    than the index of the timer array, 
    of the frame associated with this timer, 
   */

    private Timer timer[] = new Timer[NR_BUFS];        /* timer array */
    private Timer ack_timer = new Timer();

    public class FrameTimeoutTask extends TimerTask {
        int seqnr;

        //constructor
        public FrameTimeoutTask(int seq) {
            seqnr = seq;
        }

        //run will auto-exec when schedule func is called
        //implement abstract method
        public void run() {
            swe.generate_timeout_event(seqnr);
        }
    }

    private void start_timer(int seq) {
        //destroy the current timer
        stop_timer(seq);
        //reconstruct timer
        timer[seq % NR_BUFS] = new Timer();
        //FrameoutTask(seq) will be intantiated and exec run() after 300
        timer[seq % NR_BUFS].schedule(new FrameTimeoutTask(seq), 300);
    }

    private void stop_timer(int seq) {
        if (timer[seq % NR_BUFS] != null)
            timer[seq % NR_BUFS].cancel();
        timer[seq % NR_BUFS] = null;
    }

    public class AckTimeoutTask extends TimerTask {
        //implement abstract method
        public void run() {
            swe.generate_acktimeout_event();
        }
    }

    private void start_ack_timer() {
        stop_ack_timer();
        ack_timer = new Timer();
        ack_timer.schedule(new AckTimeoutTask(), 100);
    }

    private void stop_ack_timer() {
        if (ack_timer != null)
            ack_timer.cancel();
    }

}//End of class

/* Note: In class SWE, the following two public methods are available:
   . generate_acktimeout_event() and
   . generate_timeout_event(seqnr).

   To call these two methods (for implementing timers),
   the "swe" object should be referred as follows:
     swe.generate_acktimeout_event(), or
     swe.generate_timeout_event(seqnr).
*/


