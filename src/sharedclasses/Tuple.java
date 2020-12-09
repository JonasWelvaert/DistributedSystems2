package sharedclasses;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

public class Tuple implements Serializable{
	private static final long serialVersionUID = 1L;
	private byte[] cateringHash;
	private LocalDateTime t_start;
	private LocalDateTime t_stop;
	
	public Tuple(byte[] hash, LocalDateTime start, LocalDateTime stop){
		this.cateringHash = hash;
		this.t_start = start;
		this.t_stop = stop;
	}
	
	public static boolean haveOverlap(Tuple t1, Tuple t2) {
		if(!Arrays.equals(t1.cateringHash, t2.cateringHash)) {
			return false;
		}
		if(t1.t_start.isBefore(t2.t_stop) || t1.t_start.isEqual(t2.t_stop) && (t1.t_stop.isAfter(t2.t_start) || t1.t_stop.isEqual(t2.t_start))) {
			return true;
		}
		if(t2.t_start.isBefore(t1.t_stop) || t2.t_start.isEqual(t1.t_stop) && (t2.t_stop.isAfter(t1.t_start) || t2.t_stop.isEqual(t1.t_start))) {
			return true;
		}
		return false;
	}
	
	public static Tuple fixOverlap(Tuple t1, Tuple t2) {
		if(!Arrays.equals(t1.cateringHash, t2.cateringHash)) {
			return null;
		}
		LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochSecond(Math.min(t1.t_start.atZone(ZoneId.systemDefault()).toEpochSecond(), t2.t_start.atZone(ZoneId.systemDefault()).toEpochSecond())),
															ZoneId.systemDefault());
		LocalDateTime stop = LocalDateTime.ofInstant(Instant.ofEpochSecond(Math.min(t1.t_stop.atZone(ZoneId.systemDefault()).toEpochSecond(), t2.t_stop.atZone(ZoneId.systemDefault()).toEpochSecond())),
				ZoneId.systemDefault());
		
		return new Tuple(t1.cateringHash, start, stop);
	}
	
	public boolean contains(Log log) {
		Tuple temp = new Tuple(log.getHash(), log.getStartTime(), log.getEndTime());
		return haveOverlap(this, temp);
	}
	
	public String dateToString() {
		return t_start.toLocalDate().toString();
	}
}
