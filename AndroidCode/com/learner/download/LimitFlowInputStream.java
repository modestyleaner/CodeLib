package com.learner.download;

import java.io.IOException;
import java.io.InputStream;


/**
* 名称: LimitFlowInputStream.java<br>
* 描述: 下载限流<br>
* 类型: JAVA<br>
* 最近修改时间:2013-6-17 下午9:06:45<br>
* @since  2013-6-17
* @author 马全迎
*/
public class LimitFlowInputStream extends InputStream {
	public static class LimitFlow {
		/* KB */
		private static Long KB = 1024l;

		/* The smallest count chunk length in bytes */
		private static Long CHUNK_LENGTH = 1024l;

		/* How many bytes will be sent or receive */
		private int bytesWillBeSentOrReceive = 0;

		/* When the last piece was sent or receive */
		private long lastPieceSentOrReceiveTick = System.nanoTime();

		/* Default rate is 1024KB/s */
		private int maxRate = 1024;

		/* Time cost for sending CHUNK_LENGTH bytes in nanoseconds */
		private long timeCostPerChunk = (1000000000l * CHUNK_LENGTH)
				/ (this.maxRate * KB);

		/**
		 * Initialize a BandwidthLimiter object with a certain rate.
		 * 
		 * @param maxRate
		 *            the download or upload speed in KBytes
		 */
		public LimitFlow(int maxRate) {
			this.setMaxRate(maxRate);
		}

		/**
		 * Next 1 byte should do bandwidth limit.
		 */
		public synchronized void limitNextBytes() {
			this.limitNextBytes(1);
		}

		/**
		 * Next len bytes should do bandwidth limit
		 * 
		 * @param len
		 */
		public synchronized void limitNextBytes(int len) {
			this.bytesWillBeSentOrReceive += len;

			/* We have sent CHUNK_LENGTH bytes */
			while (this.bytesWillBeSentOrReceive > CHUNK_LENGTH) {
				long nowTick = System.nanoTime();
				long missedTime = this.timeCostPerChunk
						- (nowTick - this.lastPieceSentOrReceiveTick);
				if (missedTime > 0) {
					try {
						Thread.sleep(missedTime / 1000000,
								(int) (missedTime % 1000000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				this.bytesWillBeSentOrReceive -= CHUNK_LENGTH;
				this.lastPieceSentOrReceiveTick = nowTick
						+ (missedTime > 0 ? missedTime : 0);
			}
		}

		/**
		 * Set the max upload or download rate in KB/s. maxRate must be grater
		 * than 0. If maxRate is zero, it means there is no bandwidth limit.
		 * 
		 * @param maxRate
		 *            If maxRate is zero, it means there is no bandwidth limit.
		 * @throws IllegalArgumentException
		 */
		public synchronized void setMaxRate(int maxRate)
				throws IllegalArgumentException {
			if (maxRate < 0) {
				throw new IllegalArgumentException(
						"maxRate can not less than 0");
			}
			this.maxRate = maxRate < 0 ? 0 : maxRate;
			if (maxRate == 0)
				this.timeCostPerChunk = 0;
			else
				this.timeCostPerChunk = (1000000000l * CHUNK_LENGTH)
						/ (this.maxRate * KB);
		}
	}

	private InputStream input;

	private LimitFlow limiter;

	public LimitFlowInputStream(InputStream inputStream) {
		this.input = inputStream;
		this.limiter = new LimitFlow(0);
	}

	public LimitFlowInputStream(InputStream inputStream, LimitFlow limiter) {
		this.input = inputStream;
		this.limiter = limiter;
	}

	@Override
	public int read() throws IOException {
		if (this.limiter != null)
			this.limiter.limitNextBytes();
		return this.input.read();
	}

	@Override
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (limiter != null)
			limiter.limitNextBytes(len);
		return this.input.read(b, off, len);
	}
}
