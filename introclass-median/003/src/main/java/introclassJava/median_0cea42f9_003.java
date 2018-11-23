package introclassJava;

import varexc.GlobalOptions;

class IntObj {
    public int value;
    public IntObj () {
    } public IntObj (int i) {
        value = i;
    }
}

class FloatObj {
    public float value;
    public FloatObj () {
    } public FloatObj (float i) {
        value = i;
    }
}

class LongObj {
    public long value;
    public LongObj () {
    } public LongObj (long i) {
        value = i;
    }
}

class DoubleObj {
    public double value;
    public DoubleObj () {
    } public DoubleObj (double i) {
        value = i;
    }
}

class CharObj {
    public char value;
    public CharObj () {
    } public CharObj (char i) {
        value = i;
    }
}

public class median_0cea42f9_003 {
    public java.util.Scanner scanner;
    public String output = "";

    public static void main (String[]args) throws Exception {
        median_0cea42f9_003 mainClass = new median_0cea42f9_003 ();
        String output;
        if (args.length > 0) {
            mainClass.scanner = new java.util.Scanner (args[0]);
        } else {
            mainClass.scanner = new java.util.Scanner (System.in);
        }
        mainClass.exec ();
        System.out.println (mainClass.output);
    }

    public void exec () throws Exception {
        IntObj a = new IntObj (), b = new IntObj (), c = new IntObj ();
        {
			if (!varexc.GlobalOptions.c0) {
				{
					if (varexc.GlobalOptions.c1) {
						if (((c.value > a.value) && (c.value < b.value))
								|| ((c.value < a.value) && (c.value > b.value))) {
							output += (String.format("%d is the median\n", c.value));
						}
					} else {
						{
							if (varexc.GlobalOptions.c2) {
								output += (String.format("%d is the median\n", a.value));
							} else {
								{
									{
										if (varexc.GlobalOptions.c4) {
											output += (String.format("%d is the median\n", b.value));
										} else {
											{
												output += (String
														.format("Please enter 3 numbers separated by spaces > "));
												if (varexc.GlobalOptions.c5) {
													if (((b.value > a.value) && (b.value < c.value))
															|| ((b.value < a.value) && (b.value > c.value))) {
														output += (String.format("%d is the median\n", b.value));
													} else if (((c.value > a.value) && (c.value < b.value))
															|| ((c.value < a.value) && (c.value > b.value))) {
														output += (String.format("%d is the median\n", c.value));
													}
												}
											}
										}
									}
									if (varexc.GlobalOptions.c3) {
										output += (String.format("%d is the median\n", c.value));
									}
								}
							}
						}
					}
				}
			}
		}
        {
			{
				if (!varexc.GlobalOptions.c7) {
					{
						if (varexc.GlobalOptions.c8) {
							output += (String.format("%d is the median\n", b.value));
						} else {
							{
								if (varexc.GlobalOptions.c9) {
									output += (String.format("%d is the median\n", a.value));
								} else {
									{
										if (varexc.GlobalOptions.c10) {
											output += (String.format("%d is the median\n", c.value));
										} else {
											a.value = scanner.nextInt();
										}
									}
								}
							}
						}
					}
				}
			}
			if (varexc.GlobalOptions.c6) {
				output += (String.format("%d is the median\n", a.value));
			}
		}
        {
			if (!varexc.GlobalOptions.c11) {
				{
					if (varexc.GlobalOptions.c12) {
						if (((c.value > a.value) && (c.value < b.value))
								|| ((c.value < a.value) && (c.value > b.value))) {
							output += (String.format("%d is the median\n", c.value));
						}
					} else {
						{
							{
								if (varexc.GlobalOptions.c14) {
									output += (String.format("%d is the median\n", b.value));
								} else {
									{
										if (varexc.GlobalOptions.c15) {
											output += (String.format("%d is the median\n", a.value));
										} else {
											{
												if (varexc.GlobalOptions.c16) {
													if (((b.value > a.value) && (b.value < c.value))
															|| ((b.value < a.value) && (b.value > c.value))) {
														output += (String.format("%d is the median\n", b.value));
													} else if (((c.value > a.value) && (c.value < b.value))
															|| ((c.value < a.value) && (c.value > b.value))) {
														output += (String.format("%d is the median\n", c.value));
													}
												} else {
													{
														b.value = scanner.nextInt();
														if (varexc.GlobalOptions.c17) {
															output += (String.format("%d is the median\n", a.value));
														}
													}
												}
											}
										}
									}
								}
							}
							if (varexc.GlobalOptions.c13) {
								output += (String.format("%d is the median\n", b.value));
							}
						}
					}
				}
			}
		}
        {
			if (varexc.GlobalOptions.c18) {
				output += (String.format("%d is the median\n", a.value));
			} else {
				{
					{
						if (!varexc.GlobalOptions.c20) {
							{
								if (varexc.GlobalOptions.c21) {
									output += (String.format("%d is the median\n", c.value));
								} else {
									{
										if (varexc.GlobalOptions.c22) {
											output += (String.format("%d is the median\n", b.value));
										} else {
											{
												if (varexc.GlobalOptions.c23) {
													if (((b.value > a.value) && (b.value < c.value))
															|| ((b.value < a.value) && (b.value > c.value))) {
														output += (String.format("%d is the median\n", b.value));
													} else if (((c.value > a.value) && (c.value < b.value))
															|| ((c.value < a.value) && (c.value > b.value))) {
														output += (String.format("%d is the median\n", c.value));
													}
												} else {
													{
														{
															if (varexc.GlobalOptions.c25) {
																if (((c.value > a.value) && (c.value < b.value))
																		|| ((c.value < a.value)
																				&& (c.value > b.value))) {
																	output += (String.format("%d is the median\n",
																			c.value));
																}
															} else {
																c.value = scanner.nextInt();
															}
														}
														if (varexc.GlobalOptions.c24) {
															output += (String.format("%d is the median\n", b.value));
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
					if (varexc.GlobalOptions.c19) {
						if (((b.value > a.value) && (b.value < c.value))
								|| ((b.value < a.value) && (b.value > c.value))) {
							output += (String.format("%d is the median\n", b.value));
						} else if (((c.value > a.value) && (c.value < b.value))
								|| ((c.value < a.value) && (c.value > b.value))) {
							output += (String.format("%d is the median\n", c.value));
						}
					}
				}
			}
		}
        {
			if (varexc.GlobalOptions.c26) {
				if (((c.value > a.value) && (c.value < b.value)) || ((c.value < a.value) && (c.value > b.value))) {
					output += (String.format("%d is the median\n", c.value));
				}
			} else {
				{
					if (varexc.GlobalOptions.c27) {
						output += (String.format("%d is the median\n", b.value));
					} else {
						{
							{
								if (varexc.GlobalOptions.c29) {
									if (((b.value > a.value) && (b.value < c.value))
											|| ((b.value < a.value) && (b.value > c.value))) {
										output += (String.format("%d is the median\n", b.value));
									} else if (((c.value > a.value) && (c.value < b.value))
											|| ((c.value < a.value) && (c.value > b.value))) {
										output += (String.format("%d is the median\n", c.value));
									}
								} else {
									{
										{
											{
												if (!varexc.GlobalOptions.c32) {
													{
														if (((a.value > b.value) && (a.value < c.value))
																|| ((a.value < b.value) && (a.value > c.value))) {
															{
																if (!varexc.GlobalOptions.c34) {
																	{
																		{
																			{
																				if (varexc.GlobalOptions.c37) {
																					output += (String.format(
																							"%d is the median\n",
																							c.value));
																				} else {
																					{
																						{
																							if (varexc.GlobalOptions.c39) {
																								if (((b.value > a.value)
																										&& (b.value < c.value))
																										|| ((b.value < a.value)
																												&& (b.value > c.value))) {
																									output += (String
																											.format("%d is the median\n",
																													b.value));
																								} else if (((c.value > a.value)
																										&& (c.value < b.value))
																										|| ((c.value < a.value)
																												&& (c.value > b.value))) {
																									output += (String
																											.format("%d is the median\n",
																													c.value));
																								}
																							} else {
																								{
																									if (varexc.GlobalOptions.c40) {
																										output += (String
																												.format("%d is the median\n",
																														a.value));
																									} else {
																										output += (String
																												.format("%d is the median\n",
																														a.value));
																									}
																								}
																							}
																						}
																						if (varexc.GlobalOptions.c38) {
																							output += (String.format(
																									"%d is the median\n",
																									b.value));
																						}
																					}
																				}
																			}
																			if (varexc.GlobalOptions.c36) {
																				output += (String.format(
																						"%d is the median\n", a.value));
																			}
																		}
																		if (varexc.GlobalOptions.c35) {
																			if (((b.value > a.value)
																					&& (b.value < c.value))
																					|| ((b.value < a.value)
																							&& (b.value > c.value))) {
																				output += (String.format(
																						"%d is the median\n", b.value));
																			} else if (((c.value > a.value)
																					&& (c.value < b.value))
																					|| ((c.value < a.value)
																							&& (c.value > b.value))) {
																				output += (String.format(
																						"%d is the median\n", c.value));
																			}
																		}
																	}
																}
															}
														} else {
															if (!varexc.GlobalOptions.c41) {
																{
																	{
																		if (varexc.GlobalOptions.c43) {
																			output += (String.format(
																					"%d is the median\n", a.value));
																		} else {
																			{
																				if (varexc.GlobalOptions.c44) {
																					if (((b.value > a.value)
																							&& (b.value < c.value))
																							|| ((b.value < a.value)
																									&& (b.value > c.value))) {
																						output += (String.format(
																								"%d is the median\n",
																								b.value));
																					} else if (((c.value > a.value)
																							&& (c.value < b.value))
																							|| ((c.value < a.value)
																									&& (c.value > b.value))) {
																						output += (String.format(
																								"%d is the median\n",
																								c.value));
																					}
																				} else {
																					{
																						if (varexc.GlobalOptions.c45) {
																							if (((c.value > a.value)
																									&& (c.value < b.value))
																									|| ((c.value < a.value)
																											&& (c.value > b.value))) {
																								output += (String
																										.format("%d is the median\n",
																												c.value));
																							}
																						} else {
																							{
																								if (((b.value > a.value)
																										&& (b.value < c.value))
																										|| ((b.value < a.value)
																												&& (b.value > c.value))) {
																									{
																										{
																											{
																												if (varexc.GlobalOptions.c49) {
																													if (((b.value > a.value)
																															&& (b.value < c.value))
																															|| ((b.value < a.value)
																																	&& (b.value > c.value))) {
																														output += (String
																																.format("%d is the median\n",
																																		b.value));
																													} else if (((c.value > a.value)
																															&& (c.value < b.value))
																															|| ((c.value < a.value)
																																	&& (c.value > b.value))) {
																														output += (String
																																.format("%d is the median\n",
																																		c.value));
																													}
																												} else {
																													{
																														if (!varexc.GlobalOptions.c50) {
																															{
																																if (varexc.GlobalOptions.c51) {
																																	if (((c.value > a.value)
																																			&& (c.value < b.value))
																																			|| ((c.value < a.value)
																																					&& (c.value > b.value))) {
																																		output += (String
																																				.format("%d is the median\n",
																																						c.value));
																																	}
																																} else {
																																	{
																																		output += (String
																																				.format("%d is the median\n",
																																						b.value));
																																		if (varexc.GlobalOptions.c52) {
																																			if (((c.value > a.value)
																																					&& (c.value < b.value))
																																					|| ((c.value < a.value)
																																							&& (c.value > b.value))) {
																																				output += (String
																																						.format("%d is the median\n",
																																								c.value));
																																			}
																																		}
																																	}
																																}
																															}
																														}
																													}
																												}
																											}
																											if (varexc.GlobalOptions.c48) {
																												if (((b.value > a.value)
																														&& (b.value < c.value))
																														|| ((b.value < a.value)
																																&& (b.value > c.value))) {
																													output += (String
																															.format("%d is the median\n",
																																	b.value));
																												} else if (((c.value > a.value)
																														&& (c.value < b.value))
																														|| ((c.value < a.value)
																																&& (c.value > b.value))) {
																													output += (String
																															.format("%d is the median\n",
																																	c.value));
																												}
																											}
																										}
																										if (varexc.GlobalOptions.c47) {
																											output += (String
																													.format("%d is the median\n",
																															a.value));
																										}
																									}
																								} else {
																									if (!varexc.GlobalOptions.c53) {
																										{
																											{
																												{
																													if (varexc.GlobalOptions.c56) {
																														output += (String
																																.format("%d is the median\n",
																																		c.value));
																													} else {
																														{
																															{
																																{
																																	if (varexc.GlobalOptions.c59) {
																																		output += (String
																																				.format("%d is the median\n",
																																						a.value));
																																	} else {
																																		if (((c.value > a.value)
																																				&& (c.value < b.value))
																																				|| ((c.value < a.value)
																																						&& (c.value > b.value))) {
																																			{
																																				if (!varexc.GlobalOptions.c60) {
																																					{
																																						{
																																							{
																																								if (varexc.GlobalOptions.c63) {
																																									if (((b.value > a.value)
																																											&& (b.value < c.value))
																																											|| ((b.value < a.value)
																																													&& (b.value > c.value))) {
																																										output += (String
																																												.format("%d is the median\n",
																																														b.value));
																																									} else if (((c.value > a.value)
																																											&& (c.value < b.value))
																																											|| ((c.value < a.value)
																																													&& (c.value > b.value))) {
																																										output += (String
																																												.format("%d is the median\n",
																																														c.value));
																																									}
																																								} else {
																																									{
																																										{
																																											{
																																												{
																																													if (varexc.GlobalOptions.c67) {
																																														if (((c.value > a.value)
																																																&& (c.value < b.value))
																																																|| ((c.value < a.value)
																																																		&& (c.value > b.value))) {
																																															output += (String
																																																	.format("%d is the median\n",
																																																			c.value));
																																														}
																																													} else {
																																														{
																																															if (varexc.GlobalOptions.c68) {
																																																output += (String
																																																		.format("%d is the median\n",
																																																				c.value));
																																															} else {
																																																output += (String
																																																		.format("%d is the median\n",
																																																				c.value));
																																															}
																																														}
																																													}
																																												}
																																												if (varexc.GlobalOptions.c66) {
																																													if (((b.value > a.value)
																																															&& (b.value < c.value))
																																															|| ((b.value < a.value)
																																																	&& (b.value > c.value))) {
																																														output += (String
																																																.format("%d is the median\n",
																																																		b.value));
																																													} else if (((c.value > a.value)
																																															&& (c.value < b.value))
																																															|| ((c.value < a.value)
																																																	&& (c.value > b.value))) {
																																														output += (String
																																																.format("%d is the median\n",
																																																		c.value));
																																													}
																																												}
																																											}
																																											if (varexc.GlobalOptions.c65) {
																																												output += (String
																																														.format("%d is the median\n",
																																																b.value));
																																											}
																																										}
																																										if (varexc.GlobalOptions.c64) {
																																											if (((c.value > a.value)
																																													&& (c.value < b.value))
																																													|| ((c.value < a.value)
																																															&& (c.value > b.value))) {
																																												output += (String
																																														.format("%d is the median\n",
																																																c.value));
																																											}
																																										}
																																									}
																																								}
																																							}
																																							if (varexc.GlobalOptions.c62) {
																																								output += (String
																																										.format("%d is the median\n",
																																												c.value));
																																							}
																																						}
																																						if (varexc.GlobalOptions.c61) {
																																							output += (String
																																									.format("%d is the median\n",
																																											a.value));
																																						}
																																					}
																																				}
																																			}
																																		}
																																	}
																																}
																																if (varexc.GlobalOptions.c58) {
																																	if (((b.value > a.value)
																																			&& (b.value < c.value))
																																			|| ((b.value < a.value)
																																					&& (b.value > c.value))) {
																																		output += (String
																																				.format("%d is the median\n",
																																						b.value));
																																	} else if (((c.value > a.value)
																																			&& (c.value < b.value))
																																			|| ((c.value < a.value)
																																					&& (c.value > b.value))) {
																																		output += (String
																																				.format("%d is the median\n",
																																						c.value));
																																	}
																																}
																															}
																															if (varexc.GlobalOptions.c57) {
																																output += (String
																																		.format("%d is the median\n",
																																				a.value));
																															}
																														}
																													}
																												}
																												if (varexc.GlobalOptions.c55) {
																													output += (String
																															.format("%d is the median\n",
																																	b.value));
																												}
																											}
																											if (varexc.GlobalOptions.c54) {
																												output += (String
																														.format("%d is the median\n",
																																c.value));
																											}
																										}
																									}
																								}
																								if (varexc.GlobalOptions.c46) {
																									if (((b.value > a.value)
																											&& (b.value < c.value))
																											|| ((b.value < a.value)
																													&& (b.value > c.value))) {
																										output += (String
																												.format("%d is the median\n",
																														b.value));
																									} else if (((c.value > a.value)
																											&& (c.value < b.value))
																											|| ((c.value < a.value)
																													&& (c.value > b.value))) {
																										output += (String
																												.format("%d is the median\n",
																														c.value));
																									}
																								}
																							}
																						}
																					}
																				}
																			}
																		}
																	}
																	if (varexc.GlobalOptions.c42) {
																		output += (String.format("%d is the median\n",
																				a.value));
																	}
																}
															}
														}
														if (varexc.GlobalOptions.c33) {
															if (((c.value > a.value) && (c.value < b.value))
																	|| ((c.value < a.value) && (c.value > b.value))) {
																output += (String.format("%d is the median\n",
																		c.value));
															}
														}
													}
												}
											}
											if (varexc.GlobalOptions.c31) {
												if (((b.value > a.value) && (b.value < c.value))
														|| ((b.value < a.value) && (b.value > c.value))) {
													output += (String.format("%d is the median\n", b.value));
												} else if (((c.value > a.value) && (c.value < b.value))
														|| ((c.value < a.value) && (c.value > b.value))) {
													output += (String.format("%d is the median\n", c.value));
												}
											}
										}
										if (varexc.GlobalOptions.c30) {
											output += (String.format("%d is the median\n", a.value));
										}
									}
								}
							}
							if (varexc.GlobalOptions.c28) {
								output += (String.format("%d is the median\n", b.value));
							}
						}
					}
				}
			}
		}
        if (true)
            return;{
				if (varexc.GlobalOptions.c69) {
					output += (String.format("%d is the median\n", c.value));
				} else {
					{
						if (!varexc.GlobalOptions.c70) {
							{
								{
									{
										if (varexc.GlobalOptions.c73) {
											output += (String.format("%d is the median\n", b.value));
										} else {
											{
												{
													{
														if (varexc.GlobalOptions.c76) {
															if (((b.value > a.value) && (b.value < c.value))
																	|| ((b.value < a.value) && (b.value > c.value))) {
																output += (String.format("%d is the median\n",
																		b.value));
															} else if (((c.value > a.value) && (c.value < b.value))
																	|| ((c.value < a.value) && (c.value > b.value))) {
																output += (String.format("%d is the median\n",
																		c.value));
															}
														} else {
															{
																;
																if (varexc.GlobalOptions.c77) {
																	if (((b.value > a.value) && (b.value < c.value))
																			|| ((b.value < a.value)
																					&& (b.value > c.value))) {
																		output += (String.format("%d is the median\n",
																				b.value));
																	} else if (((c.value > a.value)
																			&& (c.value < b.value))
																			|| ((c.value < a.value)
																					&& (c.value > b.value))) {
																		output += (String.format("%d is the median\n",
																				c.value));
																	}
																}
															}
														}
													}
													if (varexc.GlobalOptions.c75) {
														output += (String.format("%d is the median\n", c.value));
													}
												}
												if (varexc.GlobalOptions.c74) {
													output += (String.format("%d is the median\n", b.value));
												}
											}
										}
									}
									if (varexc.GlobalOptions.c72) {
										output += (String.format("%d is the median\n", a.value));
									}
								}
								if (varexc.GlobalOptions.c71) {
									if (((c.value > a.value) && (c.value < b.value))
											|| ((c.value < a.value) && (c.value > b.value))) {
										output += (String.format("%d is the median\n", c.value));
									}
								}
							}
						}
					}
				}
			}
    }
}
