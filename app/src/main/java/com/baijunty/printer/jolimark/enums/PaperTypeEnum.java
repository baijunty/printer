package com.baijunty.printer.jolimark.enums;

/**
 * 纸张类型
 * 
 * @author Jolimark
 *
 */
public enum PaperTypeEnum {
	/**
	 * 热敏纸
	 */
	ThermalPaper(1),
	/**
	 * 标签纸
	 */
	TagPaper(2),
	/**
	 * 带孔纸
	 */
	PerforatedPaper(3);

	/**
	 * 纸张类型
	 */
	int paperType;

	private PaperTypeEnum(int p) {
		this.paperType = p;
	}

	public int getPaperType() {
		return paperType;
	}

	public void setPaperType(int paperType) {
		this.paperType = paperType;
	}
}
