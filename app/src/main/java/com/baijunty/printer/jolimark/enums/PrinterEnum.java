package com.baijunty.printer.jolimark.enums;

/**
 * 云打印机型号
 * 
 * @author Jolimark
 *
 */
public enum PrinterEnum {
	MCP58(58, 203, 1), MCP350(80, 203, 1), MCP330(80, 203, 1), MCP360(80, 203, 1), MCP610(80, 180, 1),
	MCP230(78, 203, 1), CFP535(203, 180, 2), CFP820(208, 180, 2), CLP180(110, 203, 3), CLQ200FW(110, 180, 4);

	/**
	 * 纸张宽度
	 */
	private int paperWidth;
	/**
	 * DPI
	 */
	private int DPI;
	/**
	 * 打印机分类
	 */
	private int type;

	PrinterEnum(int w, int dpi, int t) {
		this.paperWidth = w;
		this.DPI = dpi;
		this.type = t;
	}

	public int getPaperWidth() {
		return paperWidth;
	}

	public void setPaperWidth(int width) {
		this.paperWidth = width;
	}

	public int getDPI() {
		return DPI;
	}

	public void setDPI(int dPI) {
		DPI = dPI;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
