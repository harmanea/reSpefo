package cz.cuni.mff.respefo.util;

public enum FileType {
	SPECTRUM {
		@Override
		public String filterExtensions() {
			return "*.fits;*.fit;*.fts;*.asc;*.txt;*.ascii;*.rui;*.uui;*.rci;*.rfi";
		}

		@Override
		public String filterNames() {
			return "Spectrum Files";
		}
	},
	STL {
		@Override
		public String filterExtensions() {
			return "*.stl";
		}

		@Override
		public String filterNames() {
			return "Stl Files";
		}
	},
	LST {
		@Override
		public String filterExtensions() {
			return "*.lst";
		}

		@Override
		public String filterNames() {
			return "Lst Files";
		}
	},
	RVR {
		@Override
		public String filterExtensions() {
			return "*.rvr";
		}

		@Override
		public String filterNames() {
			return "Rvr Files";
		}
	},
	RVS {
		@Override
		public String filterExtensions() {
			return "*.rvs";
		}

		@Override
		public String filterNames() {
			return "Rvs Files";
		}
	};
	
	public abstract String filterNames();
	public abstract String filterExtensions();
}
