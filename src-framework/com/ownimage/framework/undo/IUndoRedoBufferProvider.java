package com.ownimage.framework.undo;

import java.util.Optional;

public interface IUndoRedoBufferProvider {

	@Deprecated
	public UndoRedoBuffer getUndoRedoBuffer();

	default Optional<UndoRedoBuffer> getOptionalUndoRedoBuffer() {
		return Optional.of(getUndoRedoBuffer());
	}
}
