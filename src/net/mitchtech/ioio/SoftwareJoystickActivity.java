package net.mitchtech.ioio;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import net.mitchtech.ioio.softwarejoystick.R;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

public class SoftwareJoystickActivity extends AbstractIOIOActivity {
	public final String TAG = SoftwareJoystickActivity.class.getSimpleName();

	private final int PAN_PIN = 3;
	private final int TILT_PIN = 6;

	private final int PWM_FREQ = 100;
	TextView txtX, txtY;
	JoystickView joystick;
	
	int tiltDegrees = 500;
	int panDegrees = 500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joystick);

		txtX = (TextView) findViewById(R.id.TextViewX);
		txtY = (TextView) findViewById(R.id.TextViewY);
		joystick = (JoystickView) findViewById(R.id.joystickView);

		joystick.setOnJostickMovedListener(_listener);
	}
	
	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		private PwmOutput panPwmOutput;
		private PwmOutput tiltPwmOutput;

		public void setup() throws ConnectionLostException {
			try {
				panPwmOutput = ioio_.openPwmOutput(new DigitalOutput.Spec(PAN_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
				tiltPwmOutput = ioio_.openPwmOutput(new DigitalOutput.Spec(TILT_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
			} catch (ConnectionLostException e) {
				throw e;
			}
		}

		public void loop() throws ConnectionLostException {
			try {
				panPwmOutput.setPulseWidth(500 + tiltDegrees * 2);
				tiltPwmOutput.setPulseWidth(500 + panDegrees * 2);
				sleep(10);
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				throw e;
			}
		}
	}
	
	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}


	private JoystickMovedListener _listener = new JoystickMovedListener() {

		@Override
		public void OnMoved(int x, int y) {
			Log.i(TAG, "x: " + x + " y: " + y);
			txtX.setText(Integer.toString(x));
			txtY.setText(Integer.toString(y));
			if (y >= 0) { 
				tiltDegrees = y * 50 + 500;
			} else {
				tiltDegrees = 500 - (Math.abs(y) *50); 
			}
			if (x >= 0) { 
				panDegrees = x * 50 + 500;
			} else {
				panDegrees = 500 - (Math.abs(x) *50); 
			}
			Log.i(TAG, "panDegrees: " + panDegrees + " tiltDegrees: " + tiltDegrees);
		}

		@Override
		public void OnReleased() {
			txtX.setText("released");
			txtY.setText("released");
		}

		public void OnReturnedToCenter() {
			txtX.setText("stopped");
			txtY.setText("stopped");
		};
	};

}
