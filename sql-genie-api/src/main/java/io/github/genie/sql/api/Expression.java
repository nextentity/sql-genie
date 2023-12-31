package io.github.genie.sql.api;

import java.io.Serializable;

sealed public interface Expression extends Serializable permits Constant, Column, Operation {
}
