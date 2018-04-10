package cz.cuni.mff.respefo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.TableHDU;

public final class SpectrumBuilder {
	
	/**
	 * Imports the spectrum from an ordinary ASCII file with the specified file path.
	 * 
	 * @param file {@code String} path to the file
	 * @return returns the {@code Spectrum} object or {@code null} if it encounters any errors
	 */
	public static Spectrum importFromASCIIFile(String file) {
		double[] XSeries;
		double[] YSeries;
		
		String name;
		
		try {
			Path p = Paths.get(file);
			List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);

			int n = lines.size();

			XSeries = new double[n];
			YSeries = new double[n];

			double X, Y;
			String[] tokens;
			int i = 0;

			for (String line : lines) {
				tokens = line.trim().replaceAll(" +", " ").split(" ");
	
				X = Double.valueOf(tokens[0]);
				Y = Double.valueOf(tokens[1]);
		
				XSeries[i] = X;
				YSeries[i++] = Y;				
			}
			
			name = p.getFileName().toString();
			int pos = name.lastIndexOf('.');
			if (pos > 0 && pos < name.length() - 1) {
				name = name.substring(0, pos);
			}
		} catch (Exception e) {
			return null;
		}
		return new Spectrum(XSeries, YSeries, name);
	}
	
	public static Spectrum importFromFitsFile(String file) {
		try (Fits f = new Fits(file);) {
			BasicHDU[] HDUs = f.read();
			
			for (int i = 0; i < HDUs.length; i++) {
				if (HDUs[i] instanceof ImageHDU) {
					ImageData imgdata = (ImageData) HDUs[i].getData();
					Object data = HDUs[i].getKernel();
					
					double[] XSeries;
					double[] YSeries;
					
					switch (HDUs[i].getBitPix()) {
					case BasicHDU.BITPIX_FLOAT:
						if (data instanceof float[]) {
							YSeries = Util.convertFloatsToDoubles((float[]) data);
						} else {
							System.out.println("[float] 2D or 3D array");
							return null;
						}
						break;
					case BasicHDU.BITPIX_DOUBLE:
						if (data instanceof double[]) {
							YSeries = (double[]) data;
						} else {
							System.out.println("[double] 2D or 3D array");
							return null;
						}
						break;
					case BasicHDU.BITPIX_INT:
						if (data instanceof int[]) {
							YSeries = Util.convertIntsToDoubles((int[]) data);
						} else {
							System.out.println("[int] 2D or 3D array");
							return null;
						}
						break;
					case BasicHDU.BITPIX_SHORT:
						if (data instanceof short[]) {
							YSeries = Util.convertShortsToDoubles((short[]) data);
						} else {
							System.out.println("[short] 2D or 3D array");
							return null;
						}
						break;
					case BasicHDU.BITPIX_LONG:
						if (data instanceof long[]) {
							YSeries = Util.convertLongsToDoubles((long[]) data);
						} else {
							System.out.println("[long] 2D or 3D array");
							return null;
						}
						break;
					case BasicHDU.BITPIX_BYTE:
						if (data instanceof byte[]) {
							YSeries = Util.convertBytesToDoubles((byte[]) data);
						} else {
							System.out.println("[byte] 2D or 3D array");
							return null;
						}
					default:
						System.out.println("not a valid value type");
						return null;
					}
					
					String name = Paths.get(file).getFileName().toString();
					int pos = name.lastIndexOf('.');
					if (pos > 0 && pos < name.length() - 1) {
						name = name.substring(0, pos);
					}
					
					double CRPIX = HDUs[i].getHeader().getDoubleValue("CRPIX" + (i + 1), 0);
					double CDELT = HDUs[i].getHeader().getDoubleValue("CDELT" + (i + 1), 1);
					double CRVAL = HDUs[i].getHeader().getDoubleValue("CRVAL" + (i + 1), 0);

					XSeries = Util.fillArray(YSeries.length, (CRPIX - 1) * CDELT + CRVAL, CDELT);
					
					return new Spectrum(XSeries, YSeries, name);
				}
			}
			
			System.out.println("No TableHDU in the FITS file");
			return null;
		} catch (FitsException e) {
			System.out.println("FitsException");
			return null;
		} catch (IOException e) {
			System.out.println("IOException");
			return null;
		} catch (ClassCastException e) {
			System.out.println("Invalid class cast");
			return null;
		}
	}
	
	private SpectrumBuilder() {}
}